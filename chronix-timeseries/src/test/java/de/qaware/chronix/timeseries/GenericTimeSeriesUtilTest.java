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


import de.qaware.chronix.timeseries.dts.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static de.qaware.chronix.timeseries.iterators.Iterators.takeAll;
import static de.qaware.chronix.timeseries.iterators.Iterators.unaryGenerator;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Basic unit test for the TimeSeriesUtil.
 */
public class GenericTimeSeriesUtilTest {


    @Test
    public void testCleanse() {

        List<Pair<Double, Double>> ts = new ArrayList<>();

        ts.add(Pair.pairOf(null, 0.0));
        ts.add(Pair.pairOf(null, 1.0));
        ts.add(Pair.pairOf(null, 2.0));  // keep last of these

        ts.add(Pair.pairOf(0.0, 2.0));
        ts.add(Pair.pairOf(0.0, 2.0));
        ts.add(Pair.pairOf(0.0, 2.0));   // skip these

        ts.add(Pair.pairOf(1.0, null));
        ts.add(Pair.pairOf(1.0, null));
        ts.add(Pair.pairOf(1.0, null));  // keep last of these

        ts.add(Pair.pairOf(2.0, 10.0));  // keep first of these
        ts.add(Pair.pairOf(3.0, 10.0));
        ts.add(Pair.pairOf(4.0, 10.0));

        // call cleanse
        Iterator<Pair<Double, Double>> result = TimeSeriesUtil.cleanse(ts.iterator());

        // unpack result
        List<Pair<Double, Double>> resultAsList = takeAll(result);

        assertThat(resultAsList.size(), is(3));
        assertThat(resultAsList.get(0), is(Pair.pairOf(null, 2.0)));
        assertThat(resultAsList.get(1), is(Pair.pairOf(1.0, null)));
        assertThat(resultAsList.get(2), is(Pair.pairOf(2.0, 10.0)));
    }


    @Test
    public void testMerge() {

        List<Pair<Double, Double>> ts1 = new ArrayList<>();
        ts1.add(Pair.pairOf(0.0, 0.0));
        ts1.add(Pair.pairOf(1.0, 10.0));
        ts1.add(Pair.pairOf(2.0, 20.0));

        List<Pair<Double, Double>> ts2 = new ArrayList<>();
        ts2.add(Pair.pairOf(0.5, 5.0));
        ts2.add(Pair.pairOf(1.5, 15.0));
        ts2.add(Pair.pairOf(2.5, 25.0));

        // put the iterators to be merged in a list
        Iterable<Iterator<Pair<Double, Double>>> input = Arrays.asList(ts1.iterator(), ts2.iterator());

        // call merge to return lists
        Iterator<Pair<Double, List<Double>>> result1 = TimeSeriesUtil.merge(input);

        // unpack the result
        List<Pair<Double, List<Double>>> resultAsList = takeAll(result1);

        assertThat(resultAsList.size(), is(6));
        assertThat(resultAsList.get(0), is(Pair.pairOf(0.0, asList(0.0, null))));
        assertThat(resultAsList.get(1), is(Pair.pairOf(0.5, asList(0.0, 5.0))));
        assertThat(resultAsList.get(2), is(Pair.pairOf(1.0, asList(10.0, 5.0))));
        assertThat(resultAsList.get(3), is(Pair.pairOf(1.5, asList(10.0, 15.0))));
        assertThat(resultAsList.get(4), is(Pair.pairOf(2.0, asList(20.0, 15.0))));
        assertThat(resultAsList.get(5), is(Pair.pairOf(2.5, asList(20.0, 25.0))));

        // rewind input
        input = Arrays.asList(ts1.iterator(), ts2.iterator());

        // call merge with operator to return doubles
        Iterator<Pair<Double, Double>> result2 = TimeSeriesUtil.merge(input, (x, y) -> x + y);

        // unpack the result
        List<Pair<Double, Double>> resultAsList2 = takeAll(result2);

        assertThat(resultAsList2.size(), is(6));
        assertThat(resultAsList2.get(0), is(Pair.pairOf(0.0, 0.0)));
        assertThat(resultAsList2.get(1), is(Pair.pairOf(0.5, 5.0)));
        assertThat(resultAsList2.get(2), is(Pair.pairOf(1.0, 15.0)));
        assertThat(resultAsList2.get(3), is(Pair.pairOf(1.5, 25.0)));
        assertThat(resultAsList2.get(4), is(Pair.pairOf(2.0, 35.0)));
        assertThat(resultAsList2.get(5), is(Pair.pairOf(2.5, 45.0)));
    }

    @Test
    public void testCompact() {

        Function<List<Double>, Double> avg = xs -> {
            double result = 0.0;
            for (Double x : xs) {
                result += x;
            }
            return result / xs.size();
        };


        List<Pair<Double, Double>> ts1 = new ArrayList<>();

        ts1.add(Pair.pairOf(-1.0, 12.0));
        ts1.add(Pair.pairOf(-1.1, 14.0));
        ts1.add(Pair.pairOf(-1.2, 16.0));   // to be skipped

        ts1.add(Pair.pairOf(0.0, 0.0));
        ts1.add(Pair.pairOf(0.1, 10.0));
        ts1.add(Pair.pairOf(0.2, 20.0));   // (0.0, 10.0)

        ts1.add(Pair.pairOf(1.0, 10.0));
        ts1.add(Pair.pairOf(1.1, 12.0));
        ts1.add(Pair.pairOf(1.2, 14.0));    // (1.0, 12.0)

        Iterator<Double> integers = unaryGenerator(0.0, x -> x + 1.0);

        // compact ts to integer timestamps
        Iterator<Pair<Double, Double>> result = TimeSeriesUtil.compact(ts1.iterator(), integers, avg);

        // unpack to List
        List<Pair<Double, Double>> resultAsList = takeAll(result);
        assertThat(resultAsList.size(), is(2));
        assertThat(resultAsList.get(0), is(Pair.pairOf(0.0, 10.0)));
        assertThat(resultAsList.get(1), is(Pair.pairOf(1.0, 12.0)));


        // test empty input
        ts1 = new ArrayList<>();
        integers = unaryGenerator(0.0, x -> x + 1.0);

        result = TimeSeriesUtil.compact(ts1.iterator(), integers, avg);

        // unpack to List
        resultAsList = takeAll(result);
        assertThat(resultAsList.size(), is(0));
    }
}
