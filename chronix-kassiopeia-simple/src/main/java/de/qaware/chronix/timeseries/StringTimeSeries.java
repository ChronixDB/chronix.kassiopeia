/*
 * Copyright (C) 2016 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.timeseries;

import de.qaware.chronix.converter.common.LongList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * A time series for storing data collected by strace, that has at least the following fields:
 * - metric name,
 * - start and end,
 * - arbitrary attributes
 * and a list of metric data points (timestamp, string)
 *
 *  This class is somehow equivalent to chronix-kassiopeia-simple -> MetricTimeSeries
 *
 * @author f.lautenschlager
 */
public final class StringTimeSeries extends AbstractTimeSeries {

    private List<String> values;

    /**
     * Private constructor.
     * To instantiate a metric time series use the builder class.
     */
    private StringTimeSeries() {
        this.timestamps = new LongList(500);
        this.values = new ArrayList<>(500);
    }

    /**
     * @return the metric data strings
     */
    public List<String> getValues() {
        return values;
    }

    /**
     *
     * @return a copy of the values as array
     */
    public String[] getValuesAsArray() {
        return values.toArray(new String[values.size()]);
    }

    /**
     * Gets the metric data string at the index i
     *
     * @param i the index position of the metric string
     * @return the metric string
     */
    public String getValue(int i) {
        return values.get(i);
    }


    /**
     * Sorts the time series values.
     */
    public void sort() {
        if (timestamps.size() > 1) {

            LongList sortedTimes = new LongList(timestamps.size());
            ArrayList<String> sortedValues = new ArrayList<>(values.size());

            points().sorted((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp())).forEachOrdered(p -> {
                sortedTimes.add(p.getTimestamp());
                sortedValues.add(p.getValue());
            });

            timestamps = sortedTimes;
            values = sortedValues;
        }
    }

    /**
     * A stream over the strace-points
     *
     * @return the points as strace-points
     */
    public Stream<StringPoint> points() {
        if (timestamps.isEmpty()) {
            return Stream.empty();
        }
        return Stream.iterate(of(0), pair -> of(pair.getIndex() + 1)).limit(timestamps.size());
    }

    private StringPoint of(int index) {
        return new StringPoint(index, timestamps.get(index), values.get(index));
    }

    /**
     * Sets the timestamps and values as data
     *
     * @param timestamps - the timestamps
     * @param values     - the values
     */
    private void setAll(LongList timestamps, List<String> values) {
        this.timestamps = timestamps;
        this.values = values;
    }

    /**
     * Adds all the given points to the time series
     *
     * @param timestamps the timestamps
     * @param values     the values
     */
    public final void addAll(List<Long> timestamps, List<String> values) {
        for (int i = 0; i < timestamps.size(); i++) {
            add(timestamps.get(i), values.get(i));
        }
    }

    /**
     * @param timestamps the timestamps as long[]
     * @param values     the values as double[]
     */
    public final void addAll(long[] timestamps, String[] values) {
        this.timestamps.addAll(timestamps);
        this.values.addAll(Arrays.asList(values));
    }

    /**
     * Adds a single timestamp and value
     *
     * @param timestamp the timestamp
     * @param value     the value
     */
    public final void add(long timestamp, String value) {
        this.timestamps.add(timestamp);
        this.values.add(value);
    }

    /**
     * Clears the time series
     */
    public void clear() {
        timestamps.clear();
        values.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        StringTimeSeries rhs = (StringTimeSeries) obj;
        return new EqualsBuilder()
                .append(this.getMetric(), rhs.getMetric())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getMetric())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("metric", metric)
                .append("attributes", attributes)
                .toString();
    }


    /**
     * The Builder class
     */
    public static final class Builder {

        /**
         * The time series object
         */
        private StringTimeSeries straceTimeSeries;

        /**
         * Constructs a new Builder
         *
         * @param metric the metric name
         */
        public Builder(String metric) {
            straceTimeSeries = new StringTimeSeries();
            straceTimeSeries.metric = metric;
        }


        /**
         * @return the filled time series
         */
        public StringTimeSeries build() {
            return straceTimeSeries;
        }


        /**
         * Sets the time series data
         *
         * @param timestamps the time stamps
         * @param values     the values
         * @return the builder
         */
        public Builder points(LongList timestamps, List<String> values) {
            if (timestamps != null && values != null) {
                straceTimeSeries.setAll(timestamps, values);
            }
            return this;
        }

        /**
         * Adds the given single data point to the time series
         *
         * @param timestamp the timestamp of the value
         * @param value     the belonging value
         * @return the builder
         */
        public Builder point(long timestamp, String value) {
            straceTimeSeries.timestamps.add(timestamp);
            straceTimeSeries.values.add(value);
            return this;
        }

        /**
         * Adds an attribute to the class
         *
         * @param key   the name of the attribute
         * @param value the value of the attribute
         * @return the builder
         */
        public Builder attribute(String key, Object value) {
            straceTimeSeries.addAttribute(key, value);
            return this;
        }

        /**
         * Sets the attributes for this time series
         *
         * @param attributes the time series attributes
         * @return the builder
         */
        public Builder attributes(Map<String, Object> attributes) {
            straceTimeSeries.attributes = attributes;
            return this;
        }

    }
}
