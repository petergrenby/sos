# Simple Object Structure
Complex data structures in Java will consume large amount memory and also put strain on the garbage collector. This is bigger issue in Java than other platforms as it has the "everything is an object" strategy. Another contributing factor is the wide use of deserialized JSON data on the JVM heap.
   
Sun/Oracle have been working on improvements to garbage collector to improve its performance, even when gigabytes of data on the heap. However, that will only ease the symptoms. Another way to reduce or even remove the issue is flatten the objects and push them outside of the heap. This is usually done by serializing them to a buffer and then copying them to an off-heap buffer. 
However, this strategy has two issues, mainly memory management and data access.
1. The issue with data management is solved by the JVM with the help of multiple garbage spaces and garbage collector. Implementing this by your self is both difficult and error prone. If this is not correctly fragmentation of memory will become a large overhead.
2. The issue with data access is that serialized data can not be accessed without deserializing it again. This is both slow and memory intensive. 

There are many project around that attempt to address the data management issue, but none (not that I know of at least) that solves the data access issue. 
So for the fun of it, I created Simple Object Structure (SOS) to solve both of them. Simple Object Structure is a flatt object structure that can be placed both on the heap or off the heap. 
It is also possible have multiple object with one memory block and still access the data within block without deserialization. Different memory management strategies can be used for allocation and pushing data into Simple Object Structures. However, access of data is implemented in a uniform way whatever strategy you use.              

## Object Handling
Simple Object Structure (SOS) is a immutable JSON like structure. It can create SOS object on-heap and off-heap.
SOS objects are really useful even if one do not push them off-heap. The main reason for this is that the overhead, compared JSON object graphs (30-40 bytes of overhead per object in the graph), is extremely small.
Even for a big JSON-structure, with list and maps in maps, there is still just a few bytes of overhead in total.

To create SOS objects helper classes JSonDataMap and JSonDataList exist. These can be used to create JSon structured data like this:
````java
JsonDataMap jdm = new JsonDataMap();
jdm.putByte("by", (byte) 64).putShort("sh", (short) 312).putInt("in", 45).putLong("lo", 76);
jdm.putString("st", "ing").putFloat("fl", 36.4f).putDouble("do", 789.45436);
````

JSon structured data can contain list:
````java
JsonDataList jdl = new JsonDataList();
jdl.addByte((byte) 1).addShort((short) 2).addInt(3).addString("elem");
jdl.addLong(4).addFloat(6.6f).addDouble(7.7);
jdm.putList("list", jdl);
````

JSon structured data can contain maps:
````java
JsonDataMap jdm2 = new JsonDataMap();
jdm2.putString("st2", "we dooo2").putString("st3", "we dooo3");
jdm.putMap("map", jdm2);
````

To create a immutable SOS-object from a JSon structure there is static build function. There is one to create SOS-object on-heap:
````java
SosByteBufferMap sos = SimpleObjectStructure.buildSosByteBuffer(jdm);
````

There is another one to create SOS-object off-heap.
````java
ByteBlockBufferManager bbbm = new ByteBlockBufferManager(1024*10);
int blockPointer = SimpleObjectStructure.buildSosByteBlockBuffer(bbbm, jdm);
SosByteBlockBufferMap sos = new SosByteBlockBufferMap(bbbm, blockPointer);
````

Values that are stored in SOS-object on-heap or off-heap can be directly access without any deserialization.
````java
byte by = sos.getByteValue("by");
short sh = sos.getShortValue("sh");
int in = sos.getIntValue("in");
long lo = sos.getLongValue("lo");
float fl = sos.getFloatValue("fl");
double do = sos.getDoubleValue("do");
String st = sos.getStringValue("st");
````

It is also possible to access lists and their values from the SOS-object.
````java
SosByteBlockBufferList sosList = sos.getListValue("list");
byte lb = sosList.getNextByteValue();
````

And it is possible to access maps and their values from the SOS-object.
````java
SosByteBlockBufferMap sosMap = sos.getMapValue("map");
String stm = sosMap.getStringValue("st2");
````

An SOS-object can lastly be extracted to JSON-object. This is important if data should updated or handled as data response object.
````java
JsonDataMap jdmEx = sos.extractJSonDataMap();
````
## Memory management
The off-heap memory management is efficient and general memory management system. It could of course be used for other use cases than SOS.  
The memory management system is called Byte Block Buffer (BBB) and is a, off-heap, block based memory area. It handles the memory area as blocks that can be split or merged to optimze usage of memory.

To get and access Byte Block Buffer one creates a Manager to handle it.
````java
ByteBlockBufferManager bbbm = new ByteBlockBufferManager(1024*10);
````

When one has an handler it is easy to allocate a portion of the off-heap memory it has. The allocation will return a pointer to the allocated block.
````java
int blockPointer = bbbm.allocate(300);
````

With help of block-pointer one can access the block and put values in to it.
````java
bbbm.putShort(blockPointer, 0, 56);
bbbm.putString(blockPointer, 0, "Test");
````

It is also possible to deallocate a block with help of the block-pointer.
````java
bbbm.deallocate(blockPointer);
````

