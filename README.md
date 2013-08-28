off-heap-hashmap
================

A Java HashMap where the values are kept off the heap in a Memory Mapped File.

I created this Map implementatio for caches where the Value objects a really BIG and just sit on your heap doing not much. By keeping them off the heap, you can have a JVM with a smaller heap size which may be better.
