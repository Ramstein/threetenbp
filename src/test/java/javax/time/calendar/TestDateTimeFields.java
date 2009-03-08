/*
 * Copyright (c) 2007-2009, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DateTimeFields.
 */
@Test
public class TestDateTimeFields {

    @SuppressWarnings("unchecked")
    private static final Map<DateTimeFieldRule, Integer> NULL_MAP = (Map) null;
    private static final DateTimeFieldRule NULL_RULE = null;
    private static final DateTimeFieldRule YEAR_RULE = ISOChronology.yearRule();
    private static final DateTimeFieldRule MOY_RULE = ISOChronology.monthOfYearRule();
    private static final DateTimeFieldRule DOM_RULE = ISOChronology.dayOfMonthRule();
    private static final DateTimeFieldRule DOY_RULE = ISOChronology.dayOfYearRule();
    private static final DateTimeFieldRule DOW_RULE = ISOChronology.dayOfWeekRule();
    private static final DateTimeFieldRule QOY_RULE = ISOChronology.quarterOfYearRule();
    private static final DateTimeFieldRule MOQ_RULE = ISOChronology.monthOfQuarterRule();
    private static final DateTimeFieldRule HOUR_RULE = ISOChronology.hourOfDayRule();
    private static final DateTimeFieldRule AMPM_RULE = ISOChronology.amPmOfDayRule();
    private static final DateTimeFieldRule HOUR_AMPM_RULE = ISOChronology.hourOfAmPmRule();
    private static final DateTimeFieldRule MIN_RULE = ISOChronology.minuteOfHourRule();
    private static final DateTimeFieldRule MILLI_RULE = ISOChronology.milliOfDayRule();

    //-----------------------------------------------------------------------
    // basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(CalendricalProvider.class.isAssignableFrom(DateTimeFields.class));
        assertTrue(DateMatcher.class.isAssignableFrom(DateTimeFields.class));
        assertTrue(TimeMatcher.class.isAssignableFrom(DateTimeFields.class));
        assertTrue(Iterable.class.isAssignableFrom(DateTimeFields.class));
        assertTrue(Serializable.class.isAssignableFrom(DateTimeFields.class));
    }

    @DataProvider(name="simple")
    Object[][] data_simple() {
        return new Object[][] {
            {DateTimeFields.fields()},
            {DateTimeFields.fields(YEAR_RULE, 2008)},
            {DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6)},
        };
    }

    @Test(dataProvider="simple")
    public void test_serialization(DateTimeFields fields) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fields);
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                baos.toByteArray()));
        if (fields.toFieldValueMap().isEmpty()) {
            assertSame(ois.readObject(), fields);
        } else {
            assertEquals(ois.readObject(), fields);
        }
    }

    public void test_immutable() {
        Class<DateTimeFields> cls = DateTimeFields.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            assertTrue(Modifier.isPrivate(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
        }
        Constructor<?>[] cons = cls.getDeclaredConstructors();
        for (Constructor<?> con : cons) {
            assertTrue(Modifier.isPrivate(con.getModifiers()));
        }
    }

    //-----------------------------------------------------------------------
    // factories
    //-----------------------------------------------------------------------
    public void factory_fields_empty() {
        DateTimeFields test = DateTimeFields.fields();
        assertEquals(test.toFieldValueMap().size(), 0);
    }

    public void factory_fields_empty_singleton() {
        assertSame(DateTimeFields.fields(), DateTimeFields.fields());
    }

    //-----------------------------------------------------------------------
    public void factory_fields_onePair() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008);
        assertFields(test, YEAR_RULE, 2008);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fields_onePair_invalidValue() {
        try {
            DateTimeFields.fields(MOY_RULE, -1);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getFieldRule(), MOY_RULE);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_onePair_null() {
        DateTimeFields.fields(NULL_RULE, 1);
    }

    //-----------------------------------------------------------------------
    public void factory_fields_twoPairs() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    public void factory_fields_twoPairs_orderNotSignificant() {
        DateTimeFields test = DateTimeFields.fields(MOY_RULE, 6, YEAR_RULE, 2008);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    public void factory_fields_twoPairs_sameFieldOverwrites() {
        DateTimeFields test = DateTimeFields.fields(MOY_RULE, 6, MOY_RULE, 7);
        assertFields(test, MOY_RULE, 7);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void factory_fields_twoPairs_invalidValue() {
        try {
            DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, -1);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getFieldRule(), MOY_RULE);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_twoPairs_nullFirst() {
        DateTimeFields.fields(NULL_RULE, 1, MOY_RULE, 6);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_twoPairs_nullSecond() {
        DateTimeFields.fields(MOY_RULE, 6, NULL_RULE, 1);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_twoPairs_nullBoth() {
        DateTimeFields.fields(NULL_RULE, 1, NULL_RULE, 6);
    }

    //-----------------------------------------------------------------------
    public void factory_fields_map() {
        // using Hashtable checks for incorrect null handling
        Map<DateTimeFieldRule, Integer> map = new Hashtable<DateTimeFieldRule, Integer>();
        map.put(YEAR_RULE, 2008);
        map.put(MOY_RULE, 6);
        DateTimeFields test = DateTimeFields.fields(map);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    public void factory_fields_map_cloned() {
        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
        map.put(YEAR_RULE, 2008);
        DateTimeFields test = DateTimeFields.fields(map);
        assertFields(test, YEAR_RULE, 2008);
        map.put(MOY_RULE, 6);
        assertFields(test, YEAR_RULE, 2008);
    }

    public void factory_fields_map_empty_singleton() {
        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
        assertSame(DateTimeFields.fields(map), DateTimeFields.fields());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_map_null() {
        DateTimeFields.fields(NULL_MAP);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_map_nullKey() {
        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
        map.put(YEAR_RULE, 2008);
        map.put(null, 6);
        DateTimeFields.fields(map);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_fields_map_nullValue() {
        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
        map.put(YEAR_RULE, 2008);
        map.put(MOY_RULE, null);
        DateTimeFields.fields(map);
    }

    //-----------------------------------------------------------------------
    // size()
    //-----------------------------------------------------------------------
    public void test_size0() {
        DateTimeFields test = DateTimeFields.fields();
        assertEquals(test.size(), 0);
    }

    public void test_size1() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008);
        assertEquals(test.size(), 1);
    }

    public void test_size2() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.size(), 2);
    }

    //-----------------------------------------------------------------------
    // iterator()
    //-----------------------------------------------------------------------
    public void test_iterator0() {
        DateTimeFields test = DateTimeFields.fields();
        Iterator<DateTimeFieldRule> iterator = test.iterator();
        assertEquals(iterator.hasNext(), false);
    }

    public void test_iterator2() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        Iterator<DateTimeFieldRule> iterator = test.iterator();
        assertEquals(iterator.hasNext(), true);
        assertEquals(iterator.next(), YEAR_RULE);
        assertEquals(iterator.hasNext(), true);
        assertEquals(iterator.next(), MOY_RULE);
        assertEquals(iterator.hasNext(), false);
    }

    //-----------------------------------------------------------------------
    // contains()
    //-----------------------------------------------------------------------
    public void test_contains() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.contains(YEAR_RULE), true);
        assertEquals(test.contains(MOY_RULE), true);
    }

    public void test_contains_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.contains(NULL_RULE), false);
    }

    public void test_contains_fieldNotPresent() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.contains(DOM_RULE), false);
    }

    //-----------------------------------------------------------------------
    // get()
    //-----------------------------------------------------------------------
    public void test_get() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.get(YEAR_RULE), 2008);
        assertEquals(test.get(MOY_RULE), 6);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_get_illegalValue() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 0);
        try {
            test.get(MOY_RULE);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getFieldRule(), MOY_RULE);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_get_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        test.get(NULL_RULE);
    }

    @Test(expectedExceptions=UnsupportedCalendarFieldException.class)
    public void test_get_fieldNotPresent() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        try {
            test.get(DOM_RULE);
        } catch (UnsupportedCalendarFieldException ex) {
            assertEquals(ex.getFieldRule(), DOM_RULE);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // getQuiet()
    //-----------------------------------------------------------------------
    public void test_getQuiet() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.getQuiet(YEAR_RULE), Integer.valueOf(2008));
        assertEquals(test.getQuiet(MOY_RULE), Integer.valueOf(6));
    }

    public void test_getQuiet_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.getQuiet(NULL_RULE), null);
    }

    public void test_getQuiet_fieldNotPresent() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(test.getQuiet(DOM_RULE), null);
    }

    //-----------------------------------------------------------------------
    // with()
    //-----------------------------------------------------------------------
    public void test_with() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.with(DOM_RULE, 30);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
        // check original immutable
        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_with_invalidValue() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        try {
            base.with(DOM_RULE, -1);
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getFieldRule(), DOM_RULE);
            throw ex;
        }
    }

    public void test_with_sameFieldOverwrites() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.with(MOY_RULE, 1);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 1);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        test.with(NULL_RULE, 30);
    }

//    //-----------------------------------------------------------------------
//    // with(Map)
//    //-----------------------------------------------------------------------
//    public void test_with_map() {
//        // using Hashtable checks for incorrect null checking
//        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new Hashtable<DateTimeFieldRule, Integer>();
//        map.put(DOM_RULE, 30);
//        DateTimeFields test = base.with(map);
//        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
//        // check original immutable
//        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6);
//    }
//
//    public void test_with_map_empty() {
//        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        DateTimeFields test = base.with(map);
//        assertSame(test, base);
//    }
//
//    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
//    public void test_with_map_invalidValue() {
//        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        map.put(DOM_RULE, -1);
//        try {
//            base.with(map);
//        } catch (IllegalCalendarFieldValueException ex) {
//            assertEquals(ex.getFieldRule(), DOM_RULE);
//            assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6);
//            throw ex;
//        }
//    }
//
//    public void test_with_map_sameFieldOverwrites() {
//        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        map.put(MOY_RULE, 1);
//        DateTimeFields test = base.with(map);
//        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 1);
//    }
//
//    @Test(expectedExceptions=NullPointerException.class)
//    public void test_with_map_null() {
//        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        test.with(NULL_MAP);
//    }
//
//    @Test(expectedExceptions=NullPointerException.class)
//    public void test_with_map_nullKey() {
//        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        map.put(null, 1);
//        test.with(map);
//    }
//
//    @Test(expectedExceptions=NullPointerException.class)
//    public void test_with_map_nullValue() {
//        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        map.put(DOM_RULE, null);
//        test.with(map);
//    }
//
//    @Test(expectedExceptions=NullPointerException.class)
//    public void test_with_map_nullBoth() {
//        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
//        Map<DateTimeFieldRule, Integer> map = new HashMap<DateTimeFieldRule, Integer>();
//        map.put(null, null);
//        test.with(map);
//    }

    //-----------------------------------------------------------------------
    // with(DateTimeFields)
    //-----------------------------------------------------------------------
    public void test_with_fields() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields fields = DateTimeFields.fields(DOM_RULE, 30);
        DateTimeFields test = base.with(fields);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
        // check original immutable
        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    public void test_with_fields_sameFieldOverwrites() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields fields = DateTimeFields.fields(MOY_RULE, 1);
        DateTimeFields test = base.with(fields);
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 1);
    }

    public void test_with_fields_self() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.with(base);
        assertSame(test, base);
    }

    public void test_with_fields_emptyAdd() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.with(DateTimeFields.fields());
        assertSame(test, base);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_with_fields_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields fields = null;
        test.with(fields);
    }

    //-----------------------------------------------------------------------
    // withFieldRemoved()
    //-----------------------------------------------------------------------
    public void test_withFieldRemoved() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.withFieldRemoved(MOY_RULE);
        assertFields(test, YEAR_RULE, 2008);
        // check original immutable
        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6);
    }

    public void test_withFieldRemoved_fieldNotPresent() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields test = base.withFieldRemoved(DOM_RULE);
        assertSame(test, base);
    }

    public void test_withFieldRemoved_emptySingleton() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008);
        DateTimeFields test = base.withFieldRemoved(YEAR_RULE);
        assertSame(test, DateTimeFields.fields());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withFieldRemoved_null() {
        DateTimeFields test = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        test.withFieldRemoved(NULL_RULE);
    }

    //-----------------------------------------------------------------------
    // matchesDate()
    //-----------------------------------------------------------------------
    public void test_matchesDate() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30);
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), true);
        // check original immutable
        assertFields(test, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
    }

    public void test_matchesDate_dowMatches() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30)
            .with(DOW_RULE, 1);
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), true);
    }

    public void test_matchesDate_dowNotMatches() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30)
            .with(DOW_RULE, 2);  // 2008-06-30 is Monday not Tuesday
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), false);
    }

    public void test_matchesDate_partialMatch() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6);
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), true);
    }

    public void test_matchesDate_timeIgnored() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30)
            .with(HOUR_RULE, 12);
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), true);
    }

    public void test_matchesDate_invalidDay() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 31);
        LocalDate date = LocalDate.date(2008, 6, 30);
        assertEquals(test.matchesDate(date), false);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesDate_null() {
        DateTimeFields test = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30);
        test.matchesDate((LocalDate) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesDate_null_emptyFields() {
        DateTimeFields test = DateTimeFields.fields();
        test.matchesDate((LocalDate) null);
    }

    //-----------------------------------------------------------------------
    // matchesTime()
    //-----------------------------------------------------------------------
    public void test_matchesTime() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11)
            .with(MIN_RULE, 30);
        LocalTime time = LocalTime.time(11, 30);
        assertEquals(test.matchesTime(time), true);
        // check original immutable
        assertFields(test, HOUR_RULE, 11, MIN_RULE, 30);
    }

    public void test_matchesTime_amPmMatches() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11)
            .with(MIN_RULE, 30)
            .with(AMPM_RULE, 0);
        LocalTime time = LocalTime.time(11, 30);
        assertEquals(test.matchesTime(time), true);
    }

    public void test_matchesTime_amPmNotMatches() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11)
            .with(MIN_RULE, 30)
            .with(AMPM_RULE, 1);  // time is 11:30, but this says PM
        LocalTime time = LocalTime.time(11, 30);
        assertEquals(test.matchesTime(time), false);
    }

    public void test_matchesTime_partialMatch() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11);
        LocalTime time = LocalTime.time(11, 30);
        assertEquals(test.matchesTime(time), true);
    }

    public void test_matchesTime_dateIgnored() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11)
            .with(MIN_RULE, 30)
            .with(YEAR_RULE, 2008);
        LocalTime time = LocalTime.time(11, 30);
        assertEquals(test.matchesTime(time), true);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesTime_null() {
        DateTimeFields test = DateTimeFields.fields()
            .with(HOUR_RULE, 11)
            .with(MIN_RULE, 30);
        test.matchesTime((LocalTime) null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesTime_null_emptyFields() {
        DateTimeFields test = DateTimeFields.fields();
        test.matchesTime((LocalTime) null);
    }

    //-----------------------------------------------------------------------
    // toFieldValueMap()
    //-----------------------------------------------------------------------
    public void test_toFieldValueMap() {
        DateTimeFields base = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30);
        SortedMap<DateTimeFieldRule, Integer> test = base.toFieldValueMap();
        assertEquals(test.size(), 3);
        assertEquals(test.get(YEAR_RULE).intValue(), 2008);
        assertEquals(test.get(MOY_RULE).intValue(), 6);
        assertEquals(test.get(DOM_RULE).intValue(), 30);
        Iterator<DateTimeFieldRule> it = test.keySet().iterator();
        assertEquals(it.next(), YEAR_RULE);
        assertEquals(it.next(), MOY_RULE);
        assertEquals(it.next(), DOM_RULE);
        // check original immutable
        test.clear();
        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
    }

    //-----------------------------------------------------------------------
    // toCalendrical()
    //-----------------------------------------------------------------------
    public void test_toCalendrical() {
        DateTimeFields base = DateTimeFields.fields()
            .with(YEAR_RULE, 2008)
            .with(MOY_RULE, 6)
            .with(DOM_RULE, 30);
        Calendrical test = base.toCalendrical();
        assertEquals(test.getOffset(), null);
        assertEquals(test.getZone(), null);
        assertFields(test.getFieldMap().toDateTimeFields(), YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
        // check original immutable
        assertFields(base, YEAR_RULE, 2008, MOY_RULE, 6, DOM_RULE, 30);
    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals0() {
        DateTimeFields a = DateTimeFields.fields();
        DateTimeFields b = DateTimeFields.fields();
        assertEquals(a.equals(b), true);
        assertEquals(a.hashCode() == b.hashCode(), true);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals1_equal() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2008);
        assertEquals(a.equals(b), true);
        assertEquals(a.hashCode() == b.hashCode(), true);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals1_notEqualValue() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2007);
        assertEquals(a.equals(b), false);
        //assertEquals(a.hashCode() == b.hashCode(), false);  // doesn't have to be so
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals1_notEqualField() {
        DateTimeFields a = DateTimeFields.fields(MOY_RULE, 3);
        DateTimeFields b = DateTimeFields.fields(DOM_RULE, 3);
        assertEquals(a.equals(b), false);
        //assertEquals(a.hashCode() == b.hashCode(), false);  // doesn't have to be so
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals2_equal() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(a.equals(b), true);
        assertEquals(a.hashCode() == b.hashCode(), true);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals2_notEqualOneValue() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2007, MOY_RULE, 6);
        assertEquals(a.equals(b), false);
        //assertEquals(a.hashCode() == b.hashCode(), false);  // doesn't have to be so
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals2_notEqualTwoValues() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2007, MOY_RULE, 5);
        assertEquals(a.equals(b), false);
        //assertEquals(a.hashCode() == b.hashCode(), false);  // doesn't have to be so
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals2_notEqualField() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        DateTimeFields b = DateTimeFields.fields(YEAR_RULE, 2008, DOM_RULE, 6);
        assertEquals(a.equals(b), false);
        //assertEquals(a.hashCode() == b.hashCode(), false);  // doesn't have to be so
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals_otherType() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(a.equals("Rubbish"), false);
    }

    public void test_equals_null() {
        DateTimeFields a = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        assertEquals(a.equals(null), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    public void test_toString0() {
        DateTimeFields base = DateTimeFields.fields();
        String test = base.toString();
        assertEquals(test, "{}");
    }

    public void test_toString1() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008);
        String test = base.toString();
        assertEquals(test, "{ISO.Year=2008}");
    }

    public void test_toString2() {
        DateTimeFields base = DateTimeFields.fields(YEAR_RULE, 2008, MOY_RULE, 6);
        String test = base.toString();
        assertEquals(test, "{ISO.Year=2008, ISO.MonthOfYear=6}");
    }

    //-----------------------------------------------------------------------
    private void assertFields(
            DateTimeFields fields,
            DateTimeFieldRule rule1, Integer value1) {
        Map<DateTimeFieldRule, Integer> map = fields.toFieldValueMap();
        assertEquals(map.size(), 1);
        assertEquals(map.get(rule1), value1);
    }
    private void assertFields(
            DateTimeFields fields,
            DateTimeFieldRule rule1, Integer value1,
            DateTimeFieldRule rule2, Integer value2) {
        Map<DateTimeFieldRule, Integer> map = fields.toFieldValueMap();
        assertEquals(map.size(), 2);
        assertEquals(map.get(rule1), value1);
        assertEquals(map.get(rule2), value2);
    }
    private void assertFields(
            DateTimeFields fields,
            DateTimeFieldRule rule1, Integer value1,
            DateTimeFieldRule rule2, Integer value2,
            DateTimeFieldRule rule3, Integer value3) {
        Map<DateTimeFieldRule, Integer> map = fields.toFieldValueMap();
        assertEquals(map.size(), 3);
        assertEquals(map.get(rule1), value1);
        assertEquals(map.get(rule2), value2);
        assertEquals(map.get(rule3), value3);
    }
//    private void assertFields(
//            DateTimeFields fields,
//            DateTimeFieldRule rule1, Integer value1,
//            DateTimeFieldRule rule2, Integer value2,
//            DateTimeFieldRule rule3, Integer value3,
//            DateTimeFieldRule rule4, Integer value4) {
//        Map<DateTimeFieldRule, Integer> map = fields.toFieldValueMap();
//        assertEquals(map.size(), 4);
//        assertEquals(map.get(rule1), value1);
//        assertEquals(map.get(rule2), value2);
//        assertEquals(map.get(rule3), value3);
//        assertEquals(map.get(rule4), value4);
//    }
//    private void assertFields(
//            DateTimeFields fields,
//            DateTimeFieldRule rule1, Integer value1,
//            DateTimeFieldRule rule2, Integer value2,
//            DateTimeFieldRule rule3, Integer value3,
//            DateTimeFieldRule rule4, Integer value4,
//            DateTimeFieldRule rule5, Integer value5) {
//        Map<DateTimeFieldRule, Integer> map = fields.toFieldValueMap();
//        assertEquals(map.size(), 5);
//        assertEquals(map.get(rule1), value1);
//        assertEquals(map.get(rule2), value2);
//        assertEquals(map.get(rule3), value3);
//        assertEquals(map.get(rule4), value4);
//        assertEquals(map.get(rule5), value5);
//    }
//    private void assertCalendrical(
//            Calendrical test,
//            DateTimeFields fields,
//            LocalDate date,
//            LocalTime time,
//            ZoneOffset offset,
//            TimeZone zone) {
//        assertEquals(test.getFields(), fields);
//        assertEquals(test.getDate(), date);
//        assertEquals(test.getTime(), time);
//        assertEquals(test.getOffset(), offset);
//        assertEquals(test.getZone(), zone);
//        assertEquals(test.toLocalDate(), date);
//        assertEquals(test.toLocalTime(), time);
//    }
}
