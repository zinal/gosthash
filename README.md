gosthash
========

Pure Java Russian GOST 34.11-94 hash implementation.

This code has been ported from OpenSSL original implementation (written in C),
donated to OpenSSL project by Cryptocom Ltd.

A pure Java version is accompanied by a JNI library written in C.
The actually used implementation is chosen depending on the availability of a JNI library.

Pure Java version is much (3x times) slower than JNI-based for large data blocks or files.

However, when used to build hash incrementally with small block sizes, pure Java version
outperforms JNI-based due to high costs of multiple JNI calls.
