/* Copyright 2016-2018 Esteve Fernandez <esteve@apache.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ros2.rcljava.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ros2.rcljava.RCLJava;
import org.ros2.rcljava.client.Client;
import org.ros2.rcljava.concurrent.RCLFuture;
import org.ros2.rcljava.consumers.Consumer;
import org.ros2.rcljava.consumers.BiConsumer;
import org.ros2.rcljava.consumers.TriConsumer;
import org.ros2.rcljava.executors.Executor;
import org.ros2.rcljava.executors.MultiThreadedExecutor;
import org.ros2.rcljava.executors.SingleThreadedExecutor;
import org.ros2.rcljava.graph.EndpointInfo;
import org.ros2.rcljava.graph.NameAndTypes;
import org.ros2.rcljava.graph.NodeNameInfo;
import org.ros2.rcljava.node.Node;
import org.ros2.rcljava.node.NodeOptions;
import org.ros2.rcljava.publisher.Publisher;
import org.ros2.rcljava.qos.policies.Reliability;
import org.ros2.rcljava.qos.QoSProfile;
import org.ros2.rcljava.service.RMWRequestId;
import org.ros2.rcljava.service.Service;
import org.ros2.rcljava.subscription.Subscription;

public class NodeTest {
  private Node node;
  private rcljava.msg.Primitives primitives1;
  private rcljava.msg.Primitives primitives2;

  private boolean boolValue1, boolValue2;
  private byte byteValue1, byteValue2;
  private byte charValue1, charValue2;
  private float float32Value1, float32Value2;
  private double float64Value1, float64Value2;
  private byte int8Value1, int8Value2;
  private byte uint8Value1, uint8Value2;
  private short int16Value1, int16Value2;
  private short uint16Value1, uint16Value2;
  private int int32Value1, int32Value2;
  private int uint32Value1, uint32Value2;
  private long int64Value1, int64Value2;
  private long uint64Value1, uint64Value2;
  private String stringValue1, stringValue2;

  boolean checkPrimitives(rcljava.msg.Primitives primitives, boolean booleanValue, byte byteValue,
      byte charValue, float float32Value, double float64Value, byte int8Value, byte uint8Value,
      short int16Value, short uint16Value, int int32Value, int uint32Value, long int64Value,
      long uint64Value, String stringValue) {
    boolean result = true;
    result = result && (primitives.getBoolValue() == booleanValue);
    result = result && (primitives.getByteValue() == byteValue);
    result = result && (primitives.getCharValue() == charValue);
    result = result && (primitives.getFloat32Value() == float32Value);
    result = result && (primitives.getFloat64Value() == float64Value);
    result = result && (primitives.getInt8Value() == int8Value);
    result = result && (primitives.getUint8Value() == uint8Value);
    result = result && (primitives.getInt16Value() == int16Value);
    result = result && (primitives.getUint16Value() == uint16Value);
    result = result && (primitives.getInt32Value() == int32Value);
    result = result && (primitives.getUint32Value() == uint32Value);
    result = result && (primitives.getInt64Value() == int64Value);
    result = result && (primitives.getUint64Value() == uint64Value);
    result = result && (primitives.getStringValue().equals(stringValue));

    return result;
  }

  @BeforeClass
  public static void setupOnce() throws Exception {
    // Just to quiet down warnings
    org.apache.log4j.BasicConfigurator.configure();

    RCLJava.rclJavaInit();
    try
    {
      // Configure log4j. Doing this dynamically so that Android does not complain about missing
      // the log4j JARs, SLF4J uses Android's native logging mechanism instead.
      Class<?> c = Class.forName("org.apache.log4j.BasicConfigurator");
      Method m = c.getDeclaredMethod("configure", (Class<?>[]) null);
      Object o = m.invoke(null, (Object[]) null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public class TestConsumer<T> implements Consumer<T> {
    private final RCLFuture<T> future;

    TestConsumer(final RCLFuture<T> future) {
      this.future = future;
    }

    public final void accept(final T msg) {
      if (!this.future.isDone()) {
        this.future.set(msg);
      }
    }
  }

  @Before
  public void setUp() {
    node = RCLJava.createNode(
      "test_node", "/", new NodeOptions().setStartParameterServices(false));

    primitives1 = new rcljava.msg.Primitives();
    primitives2 = new rcljava.msg.Primitives();

    boolValue1 = true;
    byteValue1 = (byte) 123;
    charValue1 = (byte) '\u0012';
    float32Value1 = 12.34f;
    float64Value1 = 43.21;
    int8Value1 = (byte) -12;
    uint8Value1 = (byte) 34;
    int16Value1 = (short) -1234;
    uint16Value1 = (short) 4321;
    int32Value1 = -75536;
    uint32Value1 = 85536;
    int64Value1 = -5294967296l;
    uint64Value1 = 6294967296l;
    stringValue1 = "hello world";

    primitives1.setBoolValue(boolValue1);
    primitives1.setByteValue(byteValue1);
    primitives1.setCharValue(charValue1);
    primitives1.setFloat32Value(float32Value1);
    primitives1.setFloat64Value(float64Value1);
    primitives1.setInt8Value(int8Value1);
    primitives1.setUint8Value(uint8Value1);
    primitives1.setInt16Value(int16Value1);
    primitives1.setUint16Value(uint16Value1);
    primitives1.setInt32Value(int32Value1);
    primitives1.setUint32Value(uint32Value1);
    primitives1.setInt64Value(int64Value1);
    primitives1.setUint64Value(uint64Value1);
    primitives1.setStringValue(stringValue1);

    boolValue2 = false;
    byteValue2 = (byte) 42;
    charValue2 = (byte) '\u0021';
    float32Value2 = 13.34f;
    float64Value2 = 44.21;
    int8Value2 = (byte) -13;
    uint8Value2 = (byte) 35;
    int16Value2 = (short) -1235;
    uint16Value2 = (short) 4321;
    int32Value2 = -75536;
    uint32Value2 = 85536;
    int64Value2 = -5294967296l;
    uint64Value2 = 6294967296l;
    stringValue2 = "bye world";

    primitives2.setBoolValue(boolValue2);
    primitives2.setByteValue(byteValue2);
    primitives2.setCharValue(charValue2);
    primitives2.setFloat32Value(float32Value2);
    primitives2.setFloat64Value(float64Value2);
    primitives2.setInt8Value(int8Value2);
    primitives2.setUint8Value(uint8Value2);
    primitives2.setInt16Value(int16Value2);
    primitives2.setUint16Value(uint16Value2);
    primitives2.setInt32Value(int32Value2);
    primitives2.setUint32Value(uint32Value2);
    primitives2.setInt64Value(int64Value2);
    primitives2.setUint64Value(uint64Value2);
    primitives2.setStringValue(stringValue2);
  }

  @After
  public void tearDown() {
    node.dispose();
  }

  @AfterClass
  public static void tearDownOnce() {
    RCLJava.shutdown();
  }

  @Test
  public final void testCreate() {
    assertNotEquals(0, node.getHandle());
  }

  @Test
  public final void testPubSubStdString() throws Exception {
    Publisher<std_msgs.msg.String> publisher =
        node.<std_msgs.msg.String>createPublisher(std_msgs.msg.String.class, "test_topic_string");

    RCLFuture<std_msgs.msg.String> future =
        new RCLFuture<std_msgs.msg.String>();

    Subscription<std_msgs.msg.String> subscription =
        node.<std_msgs.msg.String>createSubscription(std_msgs.msg.String.class, "test_topic_string",
            new TestConsumer<std_msgs.msg.String>(future));

    std_msgs.msg.String msg = new std_msgs.msg.String();
    msg.setData("Hello");

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    std_msgs.msg.String value = future.get();
    assertEquals("Hello", value.getData());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubBoundedArrayNested() throws Exception {
    Publisher<rcljava.msg.BoundedArrayNested> publisher =
        node.<rcljava.msg.BoundedArrayNested>createPublisher(
            rcljava.msg.BoundedArrayNested.class, "test_topic_bounded_array_nested");

    RCLFuture<rcljava.msg.BoundedArrayNested> future =
        new RCLFuture<rcljava.msg.BoundedArrayNested>();

    Subscription<rcljava.msg.BoundedArrayNested> subscription =
        node.<rcljava.msg.BoundedArrayNested>createSubscription(
            rcljava.msg.BoundedArrayNested.class, "test_topic_bounded_array_nested",
            new TestConsumer<rcljava.msg.BoundedArrayNested>(future));

    rcljava.msg.BoundedArrayNested msg = new rcljava.msg.BoundedArrayNested();
    msg.setPrimitiveValues(Arrays.asList(new rcljava.msg.Primitives[] {primitives1, primitives2}));

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.BoundedArrayNested value = future.get();
    assertNotEquals(null, value.getPrimitiveValues());

    rcljava.msg.Primitives primitivesValue1 = value.getPrimitiveValues()[0];
    rcljava.msg.Primitives primitivesValue2 = value.getPrimitiveValues()[1];

    assertTrue(checkPrimitives(primitivesValue1, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    assertTrue(checkPrimitives(primitivesValue2, boolValue2, byteValue2, charValue2, float32Value2,
        float64Value2, int8Value2, uint8Value2, int16Value2, uint16Value2, int32Value2,
        uint32Value2, int64Value2, uint64Value2, stringValue2));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubBoundedArrayPrimitives() throws Exception {
    Publisher<rcljava.msg.BoundedArrayPrimitives> publisher =
        node.<rcljava.msg.BoundedArrayPrimitives>createPublisher(
            rcljava.msg.BoundedArrayPrimitives.class, "test_topic_bounded_array_primitives");

    RCLFuture<rcljava.msg.BoundedArrayPrimitives> future =
        new RCLFuture<rcljava.msg.BoundedArrayPrimitives>();

    Subscription<rcljava.msg.BoundedArrayPrimitives> subscription =
        node.<rcljava.msg.BoundedArrayPrimitives>createSubscription(
            rcljava.msg.BoundedArrayPrimitives.class, "test_topic_bounded_array_primitives",
            new TestConsumer<rcljava.msg.BoundedArrayPrimitives>(future));

    rcljava.msg.BoundedArrayPrimitives msg = new rcljava.msg.BoundedArrayPrimitives();

    List<Boolean> boolValues = Arrays.asList(new Boolean[] {true, false, true});
    List<Byte> byteValues = Arrays.asList(new Byte[] {123, 42});
    List<Byte> charValues = Arrays.asList(new Byte[] {'\u0012', '\u0021'});
    List<Float> float32Values = Arrays.asList(new Float[] {12.34f, 13.34f});
    List<Double> float64Values = Arrays.asList(new Double[] {43.21, 44.21});
    List<Byte> int8Values = Arrays.asList(new Byte[] {-12, -13});
    List<Byte> uint8Values = Arrays.asList(new Byte[] {34, 35});
    List<Short> int16Values = Arrays.asList(new Short[] {-1234, -1235});
    List<Short> uint16Values = Arrays.asList(new Short[] {4321, 4322});
    List<Integer> int32Values = Arrays.asList(new Integer[] {-75536, -75537});
    List<Integer> uint32Values = Arrays.asList(new Integer[] {85536, 85537});
    List<Long> int64Values = Arrays.asList(new Long[] {-5294967296l, -5294967297l});
    List<Long> uint64Values = Arrays.asList(new Long[] {6294967296l, 6294967297l});
    List<String> stringValues = Arrays.asList(new String[] {"hello world", "bye world"});

    msg.setBoolValues(boolValues);
    msg.setByteValues(byteValues);
    msg.setCharValues(charValues);
    msg.setFloat32Values(float32Values);
    msg.setFloat64Values(float64Values);
    msg.setInt8Values(int8Values);
    msg.setUint8Values(uint8Values);
    msg.setInt16Values(int16Values);
    msg.setUint16Values(uint16Values);
    msg.setInt32Values(int32Values);
    msg.setUint32Values(uint32Values);
    msg.setInt64Values(int64Values);
    msg.setUint64Values(uint64Values);
    msg.setStringValues(stringValues);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.BoundedArrayPrimitives value = future.get();

    assertEquals(boolValues, value.getBoolValuesAsList());
    assertEquals(byteValues, value.getByteValuesAsList());
    assertEquals(charValues, value.getCharValuesAsList());
    assertEquals(float32Values, value.getFloat32ValuesAsList());
    assertEquals(float64Values, value.getFloat64ValuesAsList());
    assertEquals(int8Values, value.getInt8ValuesAsList());
    assertEquals(uint8Values, value.getUint8ValuesAsList());
    assertEquals(int16Values, value.getInt16ValuesAsList());
    assertEquals(uint16Values, value.getUint16ValuesAsList());
    assertEquals(int32Values, value.getInt32ValuesAsList());
    assertEquals(uint32Values, value.getUint32ValuesAsList());
    assertEquals(int64Values, value.getInt64ValuesAsList());
    assertEquals(uint64Values, value.getUint64ValuesAsList());
    assertEquals(stringValues, value.getStringValuesAsList());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubBuiltins() throws Exception {
    Publisher<rcljava.msg.Builtins> publisher = node.<rcljava.msg.Builtins>createPublisher(
        rcljava.msg.Builtins.class, "test_topic_builtins");

    RCLFuture<rcljava.msg.Builtins> future =
        new RCLFuture<rcljava.msg.Builtins>();

    Subscription<rcljava.msg.Builtins> subscription =
        node.<rcljava.msg.Builtins>createSubscription(rcljava.msg.Builtins.class,
            "test_topic_builtins", new TestConsumer<rcljava.msg.Builtins>(future));

    rcljava.msg.Builtins msg = new rcljava.msg.Builtins();

    builtin_interfaces.msg.Duration duration = new builtin_interfaces.msg.Duration();
    duration.setSec(1234);
    duration.setNanosec(4567);

    builtin_interfaces.msg.Time time = new builtin_interfaces.msg.Time();
    time.setSec(4321);
    time.setNanosec(7654);

    msg.setDurationValue(duration);
    msg.setTimeValue(time);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.Builtins value = future.get();
    builtin_interfaces.msg.Duration durationValue = value.getDurationValue();
    builtin_interfaces.msg.Time timeValue = value.getTimeValue();

    assertEquals(1234, durationValue.getSec());
    assertEquals(4567, durationValue.getNanosec());

    assertEquals(4321, timeValue.getSec());
    assertEquals(7654, timeValue.getNanosec());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubDynamicArrayNested() throws Exception {
    Publisher<rcljava.msg.DynamicArrayNested> publisher =
        node.<rcljava.msg.DynamicArrayNested>createPublisher(
            rcljava.msg.DynamicArrayNested.class, "test_topic_dynamic_array_nested");

    RCLFuture<rcljava.msg.DynamicArrayNested> future =
        new RCLFuture<rcljava.msg.DynamicArrayNested>();

    Subscription<rcljava.msg.DynamicArrayNested> subscription =
        node.<rcljava.msg.DynamicArrayNested>createSubscription(
            rcljava.msg.DynamicArrayNested.class, "test_topic_dynamic_array_nested",
            new TestConsumer<rcljava.msg.DynamicArrayNested>(future));

    rcljava.msg.DynamicArrayNested msg = new rcljava.msg.DynamicArrayNested();
    msg.setPrimitiveValues(Arrays.asList(new rcljava.msg.Primitives[] {primitives1, primitives2}));

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.DynamicArrayNested value = future.get();
    assertNotEquals(null, value.getPrimitiveValues());

    rcljava.msg.Primitives primitivesValue1 = value.getPrimitiveValues()[0];
    rcljava.msg.Primitives primitivesValue2 = value.getPrimitiveValues()[1];

    assertTrue(checkPrimitives(primitivesValue1, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    assertTrue(checkPrimitives(primitivesValue2, boolValue2, byteValue2, charValue2, float32Value2,
        float64Value2, int8Value2, uint8Value2, int16Value2, uint16Value2, int32Value2,
        uint32Value2, int64Value2, uint64Value2, stringValue2));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubDynamicArrayPrimitives() throws Exception {
    Publisher<rcljava.msg.DynamicArrayPrimitives> publisher =
        node.<rcljava.msg.DynamicArrayPrimitives>createPublisher(
            rcljava.msg.DynamicArrayPrimitives.class, "test_topic_dynamic_array_primitives");

    RCLFuture<rcljava.msg.DynamicArrayPrimitives> future =
        new RCLFuture<rcljava.msg.DynamicArrayPrimitives>();

    Subscription<rcljava.msg.DynamicArrayPrimitives> subscription =
        node.<rcljava.msg.DynamicArrayPrimitives>createSubscription(
            rcljava.msg.DynamicArrayPrimitives.class, "test_topic_dynamic_array_primitives",
            new TestConsumer<rcljava.msg.DynamicArrayPrimitives>(future));

    rcljava.msg.DynamicArrayPrimitives msg = new rcljava.msg.DynamicArrayPrimitives();

    List<Boolean> boolValues = Arrays.asList(new Boolean[] {true, false, true});
    List<Byte> byteValues = Arrays.asList(new Byte[] {123, 42});
    List<Byte> charValues = Arrays.asList(new Byte[] {'\u0012', '\u0021'});
    List<Float> float32Values = Arrays.asList(new Float[] {12.34f, 13.34f});
    List<Double> float64Values = Arrays.asList(new Double[] {43.21, 44.21});
    List<Byte> int8Values = Arrays.asList(new Byte[] {-12, -13});
    List<Byte> uint8Values = Arrays.asList(new Byte[] {34, 35});
    List<Short> int16Values = Arrays.asList(new Short[] {-1234, -1235});
    List<Short> uint16Values = Arrays.asList(new Short[] {4321, 4322});
    List<Integer> int32Values = Arrays.asList(new Integer[] {-75536, -75537});
    List<Integer> uint32Values = Arrays.asList(new Integer[] {85536, 85537});
    List<Long> int64Values = Arrays.asList(new Long[] {-5294967296l, -5294967297l});
    List<Long> uint64Values = Arrays.asList(new Long[] {6294967296l, 6294967297l});
    List<String> stringValues = Arrays.asList(new String[] {"hello world", "bye world"});

    msg.setBoolValues(boolValues);
    msg.setByteValues(byteValues);
    msg.setCharValues(charValues);
    msg.setFloat32Values(float32Values);
    msg.setFloat64Values(float64Values);
    msg.setInt8Values(int8Values);
    msg.setUint8Values(uint8Values);
    msg.setInt16Values(int16Values);
    msg.setUint16Values(uint16Values);
    msg.setInt32Values(int32Values);
    msg.setUint32Values(uint32Values);
    msg.setInt64Values(int64Values);
    msg.setUint64Values(uint64Values);
    msg.setStringValues(stringValues);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.DynamicArrayPrimitives value = future.get();

    assertEquals(boolValues, value.getBoolValuesAsList());
    assertEquals(byteValues, value.getByteValuesAsList());
    assertEquals(charValues, value.getCharValuesAsList());
    assertEquals(float32Values, value.getFloat32ValuesAsList());
    assertEquals(float64Values, value.getFloat64ValuesAsList());
    assertEquals(int8Values, value.getInt8ValuesAsList());
    assertEquals(uint8Values, value.getUint8ValuesAsList());
    assertEquals(int16Values, value.getInt16ValuesAsList());
    assertEquals(uint16Values, value.getUint16ValuesAsList());
    assertEquals(int32Values, value.getInt32ValuesAsList());
    assertEquals(uint32Values, value.getUint32ValuesAsList());
    assertEquals(int64Values, value.getInt64ValuesAsList());
    assertEquals(uint64Values, value.getUint64ValuesAsList());
    assertEquals(stringValues, value.getStringValuesAsList());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubEmpty() throws Exception {
    Publisher<rcljava.msg.Empty> publisher =
        node.<rcljava.msg.Empty>createPublisher(rcljava.msg.Empty.class, "test_topic_empty");

    RCLFuture<rcljava.msg.Empty> future =
        new RCLFuture<rcljava.msg.Empty>();

    Subscription<rcljava.msg.Empty> subscription = node.<rcljava.msg.Empty>createSubscription(
        rcljava.msg.Empty.class, "test_topic_empty", new TestConsumer<rcljava.msg.Empty>(future));

    rcljava.msg.Empty msg = new rcljava.msg.Empty();

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.Empty value = future.get();
    assertNotEquals(null, value);

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubFieldsWithSameType() throws Exception {
    Publisher<rcljava.msg.FieldsWithSameType> publisher =
        node.<rcljava.msg.FieldsWithSameType>createPublisher(
            rcljava.msg.FieldsWithSameType.class, "test_topic_fields_with_same_type");

    RCLFuture<rcljava.msg.FieldsWithSameType> future =
        new RCLFuture<rcljava.msg.FieldsWithSameType>();

    Subscription<rcljava.msg.FieldsWithSameType> subscription =
        node.<rcljava.msg.FieldsWithSameType>createSubscription(
            rcljava.msg.FieldsWithSameType.class, "test_topic_fields_with_same_type",
            new TestConsumer<rcljava.msg.FieldsWithSameType>(future));

    rcljava.msg.FieldsWithSameType msg = new rcljava.msg.FieldsWithSameType();

    msg.setPrimitiveValues1(primitives1);
    msg.setPrimitiveValues2(primitives2);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.FieldsWithSameType value = future.get();
    assertNotEquals(null, value.getPrimitiveValues1());
    assertNotEquals(null, value.getPrimitiveValues2());

    rcljava.msg.Primitives primitivesValue1 = value.getPrimitiveValues1();
    rcljava.msg.Primitives primitivesValue2 = value.getPrimitiveValues2();

    assertTrue(checkPrimitives(primitivesValue1, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    assertTrue(checkPrimitives(primitivesValue2, boolValue2, byteValue2, charValue2, float32Value2,
        float64Value2, int8Value2, uint8Value2, int16Value2, uint16Value2, int32Value2,
        uint32Value2, int64Value2, uint64Value2, stringValue2));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubNested() throws Exception {
    Publisher<rcljava.msg.Nested> publisher =
        node.<rcljava.msg.Nested>createPublisher(rcljava.msg.Nested.class, "test_topic_nested");

    RCLFuture<rcljava.msg.Nested> future =
        new RCLFuture<rcljava.msg.Nested>();

    Subscription<rcljava.msg.Nested> subscription =
        node.<rcljava.msg.Nested>createSubscription(rcljava.msg.Nested.class, "test_topic_nested",
            new TestConsumer<rcljava.msg.Nested>(future));

    rcljava.msg.Nested msg = new rcljava.msg.Nested();
    msg.setPrimitiveValues(primitives1);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.Nested value = future.get();
    assertNotEquals(null, value.getPrimitiveValues());

    rcljava.msg.Primitives primitivesValues = value.getPrimitiveValues();

    assertTrue(checkPrimitives(primitivesValues, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubPrimitives() throws Exception {
    Publisher<rcljava.msg.Primitives> publisher = node.<rcljava.msg.Primitives>createPublisher(
        rcljava.msg.Primitives.class, "test_topic_primitives");

    RCLFuture<rcljava.msg.Primitives> future =
        new RCLFuture<rcljava.msg.Primitives>();

    Subscription<rcljava.msg.Primitives> subscription =
        node.<rcljava.msg.Primitives>createSubscription(rcljava.msg.Primitives.class,
            "test_topic_primitives", new TestConsumer<rcljava.msg.Primitives>(future));

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(primitives1);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.Primitives primitivesValues = future.get();
    assertNotEquals(null, primitivesValues);

    assertTrue(checkPrimitives(primitivesValues, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubStaticArrayNested() throws Exception {
    Publisher<rcljava.msg.StaticArrayNested> publisher =
        node.<rcljava.msg.StaticArrayNested>createPublisher(
            rcljava.msg.StaticArrayNested.class, "test_topic_static_array_nested");

    RCLFuture<rcljava.msg.StaticArrayNested> future =
        new RCLFuture<rcljava.msg.StaticArrayNested>();

    Subscription<rcljava.msg.StaticArrayNested> subscription =
        node.<rcljava.msg.StaticArrayNested>createSubscription(rcljava.msg.StaticArrayNested.class,
            "test_topic_static_array_nested",
            new TestConsumer<rcljava.msg.StaticArrayNested>(future));

    rcljava.msg.StaticArrayNested msg = new rcljava.msg.StaticArrayNested();
    msg.setPrimitiveValues(Arrays.asList(
        new rcljava.msg.Primitives[] {primitives1, primitives2, primitives1, primitives2}));

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.StaticArrayNested value = future.get();
    assertNotEquals(null, value.getPrimitiveValues());

    assertEquals(4, value.getPrimitiveValues().length);

    rcljava.msg.Primitives primitivesValue1 = value.getPrimitiveValues()[0];
    rcljava.msg.Primitives primitivesValue2 = value.getPrimitiveValues()[1];
    rcljava.msg.Primitives primitivesValue3 = value.getPrimitiveValues()[2];
    rcljava.msg.Primitives primitivesValue4 = value.getPrimitiveValues()[3];

    assertTrue(checkPrimitives(primitivesValue1, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    assertTrue(checkPrimitives(primitivesValue2, boolValue2, byteValue2, charValue2, float32Value2,
        float64Value2, int8Value2, uint8Value2, int16Value2, uint16Value2, int32Value2,
        uint32Value2, int64Value2, uint64Value2, stringValue2));

    assertTrue(checkPrimitives(primitivesValue3, boolValue1, byteValue1, charValue1, float32Value1,
        float64Value1, int8Value1, uint8Value1, int16Value1, uint16Value1, int32Value1,
        uint32Value1, int64Value1, uint64Value1, stringValue1));

    assertTrue(checkPrimitives(primitivesValue4, boolValue2, byteValue2, charValue2, float32Value2,
        float64Value2, int8Value2, uint8Value2, int16Value2, uint16Value2, int32Value2,
        uint32Value2, int64Value2, uint64Value2, stringValue2));

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubSubStaticArrayPrimitives() throws Exception {
    Publisher<rcljava.msg.StaticArrayPrimitives> publisher =
        node.<rcljava.msg.StaticArrayPrimitives>createPublisher(
            rcljava.msg.StaticArrayPrimitives.class, "test_topic_static_array_primitives");

    RCLFuture<rcljava.msg.StaticArrayPrimitives> future =
        new RCLFuture<rcljava.msg.StaticArrayPrimitives>();

    Subscription<rcljava.msg.StaticArrayPrimitives> subscription =
        node.<rcljava.msg.StaticArrayPrimitives>createSubscription(
            rcljava.msg.StaticArrayPrimitives.class, "test_topic_static_array_primitives",
            new TestConsumer<rcljava.msg.StaticArrayPrimitives>(future));

    rcljava.msg.StaticArrayPrimitives msg = new rcljava.msg.StaticArrayPrimitives();

    List<Boolean> boolValues = Arrays.asList(new Boolean[] {true, false, true});
    List<Byte> byteValues = Arrays.asList(new Byte[] {123, 42, 24});
    List<Byte> charValues = Arrays.asList(new Byte[] {'\u0012', '\u0021', '\u0008'});
    List<Float> float32Values = Arrays.asList(new Float[] {12.34f, 13.34f, 14.34f});
    List<Double> float64Values = Arrays.asList(new Double[] {43.21, 54.21, 65.21});
    List<Byte> int8Values = Arrays.asList(new Byte[] {-12, -13, -14});
    List<Byte> uint8Values = Arrays.asList(new Byte[] {34, 35, 36});
    List<Short> int16Values = Arrays.asList(new Short[] {-1234, -1235, -1236});
    List<Short> uint16Values = Arrays.asList(new Short[] {4321, 4322, 4323});
    List<Integer> int32Values = Arrays.asList(new Integer[] {-75536, -75537, -75532});
    List<Integer> uint32Values = Arrays.asList(new Integer[] {85536, 85537, 85535});
    List<Long> int64Values = Arrays.asList(new Long[] {-5294967296l, -5294967297l, -5294967295l});
    List<Long> uint64Values = Arrays.asList(new Long[] {6294967296l, 6294967297l, 6294967296l});
    List<String> stringValues =
        Arrays.asList(new String[] {"hello world", "bye world", "hey world"});

    msg.setBoolValues(boolValues);
    msg.setByteValues(byteValues);
    msg.setCharValues(charValues);
    msg.setFloat32Values(float32Values);
    msg.setFloat64Values(float64Values);
    msg.setInt8Values(int8Values);
    msg.setUint8Values(uint8Values);
    msg.setInt16Values(int16Values);
    msg.setUint16Values(uint16Values);
    msg.setInt32Values(int32Values);
    msg.setUint32Values(uint32Values);
    msg.setInt64Values(int64Values);
    msg.setUint64Values(uint64Values);
    msg.setStringValues(stringValues);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.StaticArrayPrimitives value = future.get();

    assertEquals(boolValues, value.getBoolValuesAsList());
    assertEquals(byteValues, value.getByteValuesAsList());
    assertEquals(charValues, value.getCharValuesAsList());
    assertEquals(float32Values, value.getFloat32ValuesAsList());
    assertEquals(float64Values, value.getFloat64ValuesAsList());
    assertEquals(int8Values, value.getInt8ValuesAsList());
    assertEquals(uint8Values, value.getUint8ValuesAsList());
    assertEquals(int16Values, value.getInt16ValuesAsList());
    assertEquals(uint16Values, value.getUint16ValuesAsList());
    assertEquals(int32Values, value.getInt32ValuesAsList());
    assertEquals(uint32Values, value.getUint32ValuesAsList());
    assertEquals(int64Values, value.getInt64ValuesAsList());
    assertEquals(uint64Values, value.getUint64ValuesAsList());
    assertEquals(stringValues, value.getStringValuesAsList());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubUInt32() throws Exception {
    Publisher<rcljava.msg.UInt32> publisher =
        node.<rcljava.msg.UInt32>createPublisher(rcljava.msg.UInt32.class, "test_topic_uint32");

    RCLFuture<rcljava.msg.UInt32> future =
        new RCLFuture<rcljava.msg.UInt32>();

    Subscription<rcljava.msg.UInt32> subscription =
        node.<rcljava.msg.UInt32>createSubscription(rcljava.msg.UInt32.class, "test_topic_uint32",
            new TestConsumer<rcljava.msg.UInt32>(future));

    rcljava.msg.UInt32 msg = new rcljava.msg.UInt32();
    msg.setData(12345);

    while (RCLJava.ok() && !future.isDone()) {
      publisher.publish(msg);
      RCLJava.spinOnce(node);
    }

    rcljava.msg.UInt32 value = future.get();
    assertEquals(12345, value.getData());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscription.dispose();
    assertEquals(0, subscription.getHandle());
  }

  @Test
  public final void testPubUInt32MultipleNodes() throws Exception {
    Executor executor = new MultiThreadedExecutor();

    final Node publisherNode = RCLJava.createNode("publisher_node");
    final Node subscriptionNodeOne = RCLJava.createNode("subscription_node_one");
    final Node subscriptionNodeTwo = RCLJava.createNode("subscription_node_two");

    Publisher<rcljava.msg.UInt32> publisher = publisherNode.<rcljava.msg.UInt32>createPublisher(
        rcljava.msg.UInt32.class, "test_topic_multiple");

    RCLFuture<rcljava.msg.UInt32> futureOne = new RCLFuture<rcljava.msg.UInt32>();

    Subscription<rcljava.msg.UInt32> subscriptionOne =
        subscriptionNodeOne.<rcljava.msg.UInt32>createSubscription(rcljava.msg.UInt32.class,
            "test_topic_multiple", new TestConsumer<rcljava.msg.UInt32>(futureOne));

    RCLFuture<rcljava.msg.UInt32> futureTwo = new RCLFuture<rcljava.msg.UInt32>();

    Subscription<rcljava.msg.UInt32> subscriptionTwo =
        subscriptionNodeTwo.<rcljava.msg.UInt32>createSubscription(rcljava.msg.UInt32.class,
            "test_topic_multiple", new TestConsumer<rcljava.msg.UInt32>(futureTwo));

    rcljava.msg.UInt32 msg = new rcljava.msg.UInt32();
    msg.setData(54321);

    ComposableNode composablePublisherNode = new ComposableNode() {
      public Node getNode() {
        return publisherNode;
      }
    };

    ComposableNode composableSubscriptionNodeOne = new ComposableNode() {
      public Node getNode() {
        return subscriptionNodeOne;
      }
    };

    ComposableNode composableSubscriptionNodeTwo = new ComposableNode() {
      public Node getNode() {
        return subscriptionNodeTwo;
      }
    };

    executor.addNode(composablePublisherNode);
    executor.addNode(composableSubscriptionNodeOne);
    executor.addNode(composableSubscriptionNodeTwo);

    while (RCLJava.ok() && !(futureOne.isDone() && futureTwo.isDone())) {
      publisher.publish(msg);
      executor.spinSome();
    }

    rcljava.msg.UInt32 valueOne = futureOne.get();
    assertEquals(54321, valueOne.getData());

    rcljava.msg.UInt32 valueTwo = futureTwo.get();
    assertEquals(54321, valueTwo.getData());

    publisher.dispose();
    assertEquals(0, publisher.getHandle());
    subscriptionOne.dispose();
    assertEquals(0, subscriptionOne.getHandle());
    subscriptionTwo.dispose();
    assertEquals(0, subscriptionTwo.getHandle());
    publisherNode.dispose();
    assertEquals(0, publisherNode.getHandle());
    subscriptionNodeOne.dispose();
    assertEquals(0, subscriptionNodeOne.getHandle());
    subscriptionNodeTwo.dispose();
    assertEquals(0, subscriptionNodeTwo.getHandle());
  }

  @Test
  public final void testGetNodeNames() throws Exception {
    final Node node1 = RCLJava.createNode("test_get_node_names_1");
    final Node node2 = RCLJava.createNode("test_get_node_names_2");
    final Node node3 = RCLJava.createNode("test_get_node_names_3");

    Consumer<Collection<NodeNameInfo>> validateNodeNameInfo =
    new Consumer<Collection<NodeNameInfo>>() {
      public void accept(final Collection<NodeNameInfo> nodeNamesInfo) {
        assertEquals(4, nodeNamesInfo.size());
        assertTrue(
          "node 'test_node' was not discovered",
          nodeNamesInfo.contains(new NodeNameInfo("test_node", "/", "/")));
        assertTrue(
          "node 'test_get_node_names_1' was not discovered",
          nodeNamesInfo.contains(new NodeNameInfo("test_get_node_names_1", "/", "/")));
        assertTrue(
          "node 'test_get_node_names_2' was not discovered",
          nodeNamesInfo.contains(new NodeNameInfo("test_get_node_names_1", "/", "/")));
        assertTrue(
          "node 'test_get_node_names_3' was not discovered",
          nodeNamesInfo.contains(new NodeNameInfo("test_get_node_names_1", "/", "/")));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NodeNameInfo> nodeNamesInfo = null;
    do {
      nodeNamesInfo = this.node.getNodeNames();
      try {
        validateNodeNameInfo.accept(nodeNamesInfo);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(nodeNamesInfo);
    validateNodeNameInfo.accept(nodeNamesInfo);

    node1.dispose();
    node2.dispose();
    node3.dispose();
  }

  @Test
  public final void testGetTopicNamesAndTypes() throws Exception {
    Publisher<rcljava.msg.UInt32> publisher = node.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_topic_names_and_types_one");
    Publisher<rcljava.msg.UInt32> publisher2 = node.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_topic_names_and_types_two");
    Subscription<rcljava.msg.UInt32> subscription = node.<rcljava.msg.UInt32>createSubscription(
      rcljava.msg.UInt32.class, "test_get_topic_names_and_types_one",
      new Consumer<rcljava.msg.UInt32>() {
        public void accept(final rcljava.msg.UInt32 msg) {}
      });
    Subscription<rcljava.msg.Empty> subscription2 = node.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_topic_names_and_types_three",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });

    Consumer<Collection<NameAndTypes>> validateNameAndTypes =
    new Consumer<Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> namesAndTypes) {
        // TODO(ivanpauno): Using assertj may help a lot here https://assertj.github.io/doc/.
        assertEquals(namesAndTypes.size(), 3);
        assertTrue(
          "topic 'test_get_topic_names_and_types_one' was not discovered",
          namesAndTypes.contains(
            new NameAndTypes(
              "/test_get_topic_names_and_types_one",
              Arrays.asList("rcljava/msg/UInt32"))));
        assertTrue(
          "topic 'test_get_topic_names_and_types_two' was not discovered",
          namesAndTypes.contains(
            new NameAndTypes(
              "/test_get_topic_names_and_types_two",
              Arrays.asList("rcljava/msg/UInt32"))));
        assertTrue(
          "topic 'test_get_topic_names_and_types_three' was not discovered",
          namesAndTypes.contains(
            new NameAndTypes(
              "/test_get_topic_names_and_types_three",
              Arrays.asList("rcljava/msg/Empty"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> namesAndTypes = null;
    do {
      namesAndTypes = this.node.getTopicNamesAndTypes();
      try {
        validateNameAndTypes.accept(namesAndTypes);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(namesAndTypes);
    validateNameAndTypes.accept(namesAndTypes);

    publisher.dispose();
    publisher2.dispose();
    subscription.dispose();
    subscription2.dispose();
  }

  @Test
  public final void testGetServiceNamesAndTypes() throws Exception {
    Service<rcljava.srv.AddTwoInts> service = node.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_service_names_and_types_one",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });
    Client<rcljava.srv.AddTwoInts> client = node.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_service_names_and_types_two");

    Consumer<Collection<NameAndTypes>> validateNameAndTypes =
    new Consumer<Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> namesAndTypes) {
        assertEquals(namesAndTypes.size(), 2);
        assertTrue(
          "service 'test_service_names_and_types_one' was not discovered",
          namesAndTypes.contains(
            new NameAndTypes(
              "/test_service_names_and_types_one",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
        assertTrue(
          "service 'test_service_names_and_types_two' was not discovered",
          namesAndTypes.contains(
            new NameAndTypes(
              "/test_service_names_and_types_two",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> namesAndTypes = null;
    do {
      namesAndTypes = this.node.getServiceNamesAndTypes();
      try {
        validateNameAndTypes.accept(namesAndTypes);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(namesAndTypes);
    validateNameAndTypes.accept(namesAndTypes);

    service.dispose();
    client.dispose();
  }

  @Test
  public final void testGetPublishersInfo() {
    Publisher<rcljava.msg.UInt32> publisher =
      node.<rcljava.msg.UInt32>createPublisher(rcljava.msg.UInt32.class, "test_get_publishers_info");
    Publisher<rcljava.msg.UInt32> publisher2 =
      node.<rcljava.msg.UInt32>createPublisher(
        rcljava.msg.UInt32.class, "test_get_publishers_info", QoSProfile.sensorData());

    Consumer<Collection<EndpointInfo>> validateEndpointInfo =
    new Consumer<Collection<EndpointInfo>>() {
      public void accept(final Collection<EndpointInfo> info) {
        assertEquals(info.size(), 2);
        Iterator<EndpointInfo> it = info.iterator();
        EndpointInfo item = it.next();
        assertEquals("test_node", item.nodeName);
        assertEquals("/", item.nodeNamespace);
        assertEquals("rcljava/msg/UInt32", item.topicType);
        assertEquals(item.endpointType, EndpointInfo.EndpointType.PUBLISHER);
        assertEquals(item.qos.getReliability(), Reliability.RELIABLE);
        item = it.next();
        assertEquals("test_node", item.nodeName);
        assertEquals("/", item.nodeNamespace);
        assertEquals("rcljava/msg/UInt32", item.topicType);
        assertEquals(item.endpointType, EndpointInfo.EndpointType.PUBLISHER);
        assertEquals(item.qos.getReliability(), Reliability.BEST_EFFORT);
        assertFalse(it.hasNext());
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<EndpointInfo> publishersInfo = null;
    do {
      publishersInfo = node.getPublishersInfo("/test_get_publishers_info");
      try {
        validateEndpointInfo.accept(publishersInfo);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(publishersInfo);
    validateEndpointInfo.accept(publishersInfo);
    publisher.dispose();
    publisher2.dispose();
  }

  @Test
  public final void testGetSubscriptionsInfo() {
    Subscription<rcljava.msg.UInt32> subscription = node.<rcljava.msg.UInt32>createSubscription(
      rcljava.msg.UInt32.class, "test_get_subscriptions_info", new Consumer<rcljava.msg.UInt32>() {
        public void accept(final rcljava.msg.UInt32 msg) {}
      });
    Subscription<rcljava.msg.UInt32> subscription2 = node.<rcljava.msg.UInt32>createSubscription(
      rcljava.msg.UInt32.class, "test_get_subscriptions_info", new Consumer<rcljava.msg.UInt32>() {
        public void accept(final rcljava.msg.UInt32 msg) {}
      }, QoSProfile.sensorData());

    Consumer<Collection<EndpointInfo>> validateEndpointInfo =
    new Consumer<Collection<EndpointInfo>>() {
      public void accept(final Collection<EndpointInfo> info) {
        assertEquals(info.size(), 2);
        Iterator<EndpointInfo> it = info.iterator();
        EndpointInfo item = it.next();
        assertEquals("test_node", item.nodeName);
        assertEquals("/", item.nodeNamespace);
        assertEquals("rcljava/msg/UInt32", item.topicType);
        assertEquals(item.endpointType, EndpointInfo.EndpointType.SUBSCRIPTION);
        assertEquals(item.qos.getReliability(), Reliability.RELIABLE);
        item = it.next();
        assertEquals("test_node", item.nodeName);
        assertEquals("/", item.nodeNamespace);
        assertEquals("rcljava/msg/UInt32", item.topicType);
        assertEquals(item.endpointType, EndpointInfo.EndpointType.SUBSCRIPTION);
        assertEquals(item.qos.getReliability(), Reliability.BEST_EFFORT);
        assertFalse(it.hasNext());
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<EndpointInfo> subscriptionsInfo = null;
    do {
      subscriptionsInfo = node.getSubscriptionsInfo("/test_get_subscriptions_info");
      try {
        validateEndpointInfo.accept(subscriptionsInfo);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(subscriptionsInfo);
    validateEndpointInfo.accept(subscriptionsInfo);
    subscription.dispose();
    subscription2.dispose();
  }

  @Test
  public final void testGetPublisherNamesAndTypesByNode() throws Exception {
    final Node remoteNode = RCLJava.createNode("test_get_publisher_names_and_types_remote_node");
    Publisher<rcljava.msg.UInt32> publisher1 = node.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_publisher_names_and_types_one");
    Publisher<rcljava.msg.UInt32> publisher2 = node.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_publisher_names_and_types_two");
    Publisher<rcljava.msg.UInt32> publisher3 = remoteNode.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_publisher_names_and_types_two");
    Publisher<rcljava.msg.UInt32> publisher4 = remoteNode.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_publisher_names_and_types_three");
    Subscription<rcljava.msg.Empty> subscription = node.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_topic_names_and_types_this_should_not_appear",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });

    BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>> validateNameAndTypes =
    new BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> local, Collection<NameAndTypes> remote) {
        // TODO(ivanpauno): Using assertj may help a lot here https://assertj.github.io/doc/.
        assertEquals(local.size(), 2);
        assertTrue(
          "topic 'test_get_publisher_names_and_types_one' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_publisher_names_and_types_one",
              Arrays.asList("rcljava/msg/UInt32"))));
        assertTrue(
          "topic 'test_get_publisher_names_and_types_two' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_publisher_names_and_types_two",
              Arrays.asList("rcljava/msg/UInt32"))));

        assertEquals(remote.size(), 2);
        assertTrue(
          "topic 'test_get_publisher_names_and_types_two' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_publisher_names_and_types_two",
              Arrays.asList("rcljava/msg/UInt32"))));
        assertTrue(
          "topic 'test_get_publisher_names_and_types_three' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_publisher_names_and_types_three",
              Arrays.asList("rcljava/msg/UInt32"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> local = null;
    Collection<NameAndTypes> remote = null;
    do {
      local = this.node.getPublisherNamesAndTypesByNode("test_node", "/");
      remote = this.node.getPublisherNamesAndTypesByNode(
        "test_get_publisher_names_and_types_remote_node", "/");
      try {
        validateNameAndTypes.accept(local, remote);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(local);
    assertNotNull(remote);
    validateNameAndTypes.accept(local, remote);

    publisher1.dispose();
    publisher2.dispose();
    publisher3.dispose();
    publisher4.dispose();
    subscription.dispose();
    remoteNode.dispose();
  }

  @Test
  public final void testGetSubscriptionNamesAndTypesByNode() throws Exception {
    final Node remoteNode = RCLJava.createNode("test_get_subscription_names_and_types_remote_node");
    Subscription<rcljava.msg.Empty> subscription1 = node.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_subscription_names_and_types_one",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });
    Subscription<rcljava.msg.Empty> subscription2 = node.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_subscription_names_and_types_two",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });
    Subscription<rcljava.msg.Empty> subscription3 = remoteNode.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_subscription_names_and_types_two",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });
    Subscription<rcljava.msg.Empty> subscription4 = remoteNode.<rcljava.msg.Empty>createSubscription(
      rcljava.msg.Empty.class, "test_get_subscription_names_and_types_three",
      new Consumer<rcljava.msg.Empty>() {
        public void accept(final rcljava.msg.Empty msg) {}
      });
    Publisher<rcljava.msg.UInt32> publisher = node.<rcljava.msg.UInt32>createPublisher(
      rcljava.msg.UInt32.class, "test_get_topic_names_and_types_this_should_not_appear");

    BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>> validateNameAndTypes =
    new BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> local, Collection<NameAndTypes> remote) {
        // TODO(ivanpauno): Using assertj may help a lot here https://assertj.github.io/doc/.
        assertEquals(local.size(), 2);
        assertTrue(
          "topic 'test_get_subscription_names_and_types_one' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_subscription_names_and_types_one",
              Arrays.asList("rcljava/msg/Empty"))));
        assertTrue(
          "topic 'test_get_subscription_names_and_types_two' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_subscription_names_and_types_two",
              Arrays.asList("rcljava/msg/Empty"))));

        assertEquals(remote.size(), 2);
        assertTrue(
          "topic 'test_get_subscription_names_and_types_two' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_subscription_names_and_types_two",
              Arrays.asList("rcljava/msg/Empty"))));
        assertTrue(
          "topic 'test_get_subscription_names_and_types_three' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_subscription_names_and_types_three",
              Arrays.asList("rcljava/msg/Empty"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> local = null;
    Collection<NameAndTypes> remote = null;
    do {
      local = this.node.getSubscriptionNamesAndTypesByNode("test_node", "/");
      remote = this.node.getSubscriptionNamesAndTypesByNode(
        "test_get_subscription_names_and_types_remote_node", "/");
      try {
        validateNameAndTypes.accept(local, remote);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(local);
    assertNotNull(remote);
    validateNameAndTypes.accept(local, remote);

    subscription1.dispose();
    subscription2.dispose();
    subscription3.dispose();
    subscription4.dispose();
    publisher.dispose();
    remoteNode.dispose();
  }

  @Test
  public final void testGetServiceNamesAndTypesByNode() throws Exception {
    final Node remoteNode = RCLJava.createNode(
      "test_get_service_names_and_types_remote_node", "/",
      new NodeOptions().setStartParameterServices(false));
    Service<rcljava.srv.AddTwoInts> service1 = node.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_get_service_names_and_types_one",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });
    Service<rcljava.srv.AddTwoInts> service2 = node.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_get_service_names_and_types_two",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });
    Service<rcljava.srv.AddTwoInts> service3 = remoteNode.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_get_service_names_and_types_two",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });
    Service<rcljava.srv.AddTwoInts> service4 = remoteNode.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_get_service_names_and_types_three",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });
    Client<rcljava.srv.AddTwoInts> client = node.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_get_service_names_and_types_this_should_not_appear");

    BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>> validateNameAndTypes =
    new BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> local, Collection<NameAndTypes> remote) {
        // TODO(ivanpauno): Using assertj may help a lot here https://assertj.github.io/doc/.
        assertEquals(local.size(), 2);
        assertTrue(
          "service 'test_get_service_names_and_types_one' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_service_names_and_types_one",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
        assertTrue(
          "service 'test_get_service_names_and_types_two' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_service_names_and_types_two",
              Arrays.asList("rcljava/srv/AddTwoInts"))));

        assertEquals(remote.size(), 2);
        assertTrue(
          "service 'test_get_service_names_and_types_two' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_service_names_and_types_two",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
        assertTrue(
          "service 'test_get_service_names_and_types_three' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_service_names_and_types_three",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> local = null;
    Collection<NameAndTypes> remote = null;
    do {
      local = this.node.getServiceNamesAndTypesByNode("test_node", "/");
      remote = this.node.getServiceNamesAndTypesByNode(
        "test_get_service_names_and_types_remote_node", "/");
      try {
        validateNameAndTypes.accept(local, remote);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(local);
    assertNotNull(remote);
    validateNameAndTypes.accept(local, remote);

    service1.dispose();
    service2.dispose();
    service3.dispose();
    service4.dispose();
    client.dispose();
    remoteNode.dispose();
  }

  @Test
  public final void testGetClientNamesAndTypesByNode() throws Exception {
    final Node remoteNode = RCLJava.createNode(
      "test_get_client_names_and_types_remote_node", "/",
      new NodeOptions().setStartParameterServices(false));
    Client<rcljava.srv.AddTwoInts> client1 = node.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_get_client_names_and_types_one");
    Client<rcljava.srv.AddTwoInts> client2 = node.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_get_client_names_and_types_two");
    Client<rcljava.srv.AddTwoInts> client3 = remoteNode.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_get_client_names_and_types_two");
    Client<rcljava.srv.AddTwoInts> client4 = remoteNode.<rcljava.srv.AddTwoInts>createClient(
      rcljava.srv.AddTwoInts.class, "test_get_client_names_and_types_three");
    Service<rcljava.srv.AddTwoInts> service = node.<rcljava.srv.AddTwoInts>createService(
      rcljava.srv.AddTwoInts.class, "test_get_client_names_and_types_this_should_not_appear",
      new TriConsumer<
        RMWRequestId, rcljava.srv.AddTwoInts_Request, rcljava.srv.AddTwoInts_Response>()
      {
        public final void accept(
          final RMWRequestId header,
          final rcljava.srv.AddTwoInts_Request request,
          final rcljava.srv.AddTwoInts_Response response)
        {}
      });

    BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>> validateNameAndTypes =
    new BiConsumer<Collection<NameAndTypes>, Collection<NameAndTypes>>() {
      public void accept(final Collection<NameAndTypes> local, Collection<NameAndTypes> remote) {
        // TODO(ivanpauno): Using assertj may help a lot here https://assertj.github.io/doc/.
        assertEquals(local.size(), 2);
        assertTrue(
          "client 'test_get_client_names_and_types_one' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_client_names_and_types_one",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
        assertTrue(
          "client 'test_get_client_names_and_types_two' was not discovered for local node",
          local.contains(
            new NameAndTypes(
              "/test_get_client_names_and_types_two",
              Arrays.asList("rcljava/srv/AddTwoInts"))));

        assertEquals(remote.size(), 2);
        assertTrue(
          "client 'test_get_client_names_and_types_two' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_client_names_and_types_two",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
        assertTrue(
          "client 'test_get_client_names_and_types_three' was not discovered for remote node",
          remote.contains(
            new NameAndTypes(
              "/test_get_client_names_and_types_three",
              Arrays.asList("rcljava/srv/AddTwoInts"))));
      }
    };

    long start = System.currentTimeMillis();
    boolean ok = false;
    Collection<NameAndTypes> local = null;
    Collection<NameAndTypes> remote = null;
    do {
      local = this.node.getClientNamesAndTypesByNode("test_node", "/");
      remote = this.node.getClientNamesAndTypesByNode(
        "test_get_client_names_and_types_remote_node", "/");
      try {
        validateNameAndTypes.accept(local, remote);
        ok = true;
      } catch (AssertionError err) {
        // ignore here, it's going to be validated again at the end.
      }
      // TODO(ivanpauno): We could wait for the graph guard condition to be triggered if that
      // would be available.
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException err) {
        // ignore
      }
    } while (!ok && System.currentTimeMillis() < start + 1000);
    assertNotNull(local);
    assertNotNull(remote);
    validateNameAndTypes.accept(local, remote);

    client1.dispose();
    client2.dispose();
    client3.dispose();
    client4.dispose();
    service.dispose();
    remoteNode.dispose();
  }
}
