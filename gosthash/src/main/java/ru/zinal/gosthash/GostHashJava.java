// $Id: GostHashJava.java 45536 2013-04-08 13:23:58Z zinal $
package ru.zinal.gosthash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Реализация расчета хеш-суммы ГОСТ 34.11-94 на Java.
 * @author zinal
 * @since 2011-01-13
 */
public class GostHashJava implements GostHashIface {

  private final static long TOP_UINT = 0xFFFFFFFFL + 1L;
  static long int2ulong(int n) {
    if ( n >= 0 )
      return (long) n;
    return TOP_UINT + n;
  }
  private final static int TOP_UBYTE = 0xFF + 1;
  static int byte2uint(byte n) {
    if ( n >= 0 )
      return (int) n;
    return TOP_UBYTE + n;
  }

  //----------------------------------------------------------------------------------

  /* Xor two sequences of bytes */
  static void xor_blocks (byte[] result, byte[] a, byte[] b, int bstart, int len) {
    int i;
    for (i=0; i<len; ++i)
      result[i] = (byte) (a[i] ^ b[bstart+i]);
  }

  static void swap_bytes (byte[] w, byte[] k) {
    for (int i=0; i<4; ++i)	{
      for (int j=0; j<8; ++j)
          k[i+4*j] = w[8*i+j];
    }
  }

  /* was A_A */
  static void circle_xor8 (byte[] w, byte[] k) {
    circle_xor8(w, 0, k);
  }
  static void circle_xor8 (byte[] w, int wstart, byte[] k) {
    final byte[] buf = new byte[8];
    System.arraycopy(w, wstart, buf, 0, 8);
    System.arraycopy(w, wstart+8, k, 0, 24);
    for (int i=0; i<8; ++i)
      k[i+24] = (byte) (buf[i] ^ k[i]);
  }

  /* was R_R */
  static void transform_3 (byte[] data) {
    final int acc =
        (byte2uint(data[0]) ^ byte2uint(data[2]) ^
         byte2uint(data[4]) ^ byte2uint(data[6]) ^
         byte2uint(data[24]) ^ byte2uint(data[30])) |
       ((byte2uint(data[1]) ^ byte2uint(data[3]) ^
         byte2uint(data[5]) ^ byte2uint(data[7]) ^
         byte2uint(data[25]) ^ byte2uint(data[31])) << 8);
    System.arraycopy(data, 2, data, 0, 30);
    data[30] = (byte) (acc & 0xff);
    data[31] = (byte) (acc >>> 8);
  }

  /* Adds blocks of N bytes modulo 2**(8*n). Returns carry*/
  static int addBlocks(int n, byte[] left, byte[] right, int rightPos) {
    int i;
    int carry = 0;
    int sum;
    for (i=0;i<n;i++)  {
      sum = byte2uint(left[i]) + byte2uint(right[rightPos+i]) + carry;
      left[i] = (byte) (sum & 0xff);
      carry = sum>>>8;
    }
    return carry;
  }

  //----------------------------------------------------------------------------------

  /* Internal representation of GOST substitution blocks */
  private static final class GostSubstBlock {
    final byte k8[] = new byte[16];
    final byte k7[] = new byte[16];
    final byte k6[] = new byte[16];
    final byte k5[] = new byte[16];
    final byte k4[] = new byte[16];
    final byte k3[] = new byte[16];
    final byte k2[] = new byte[16];
    final byte k1[] = new byte[16];
  }

  /* Substitution blocks for hash function 1.2.643.2.9.1.6.1  */
  private final static GostSubstBlock GostR3411_94_CryptoProParamSet = new GostSubstBlock();
  static {
    byte[] k8 = new byte[] {0x1,0x3,0xA,0x9,0x5,0xB,0x4,0xF,0x8,0x6,0x7,0xE,0xD,0x0,0x2,0xC};
    byte[] k7 = new byte[] {0xD,0xE,0x4,0x1,0x7,0x0,0x5,0xA,0x3,0xC,0x8,0xF,0x6,0x2,0x9,0xB};
    byte[] k6 = new byte[] {0x7,0x6,0x2,0x4,0xD,0x9,0xF,0x0,0xA,0x1,0x5,0xB,0x8,0xE,0xC,0x3};
    byte[] k5 = new byte[] {0x7,0x6,0x4,0xB,0x9,0xC,0x2,0xA,0x1,0x8,0x0,0xE,0xF,0xD,0x3,0x5};
    byte[] k4 = new byte[] {0x4,0xA,0x7,0xC,0x0,0xF,0x2,0x8,0xE,0x1,0x6,0x5,0xD,0xB,0x9,0x3};
    byte[] k3 = new byte[] {0x7,0xF,0xC,0xE,0x9,0x4,0x1,0x0,0x3,0xB,0x5,0x2,0x6,0xA,0x8,0xD};
    byte[] k2 = new byte[] {0x5,0xF,0x4,0x0,0x2,0xD,0xB,0x9,0x1,0x7,0x6,0x3,0xC,0xE,0xA,0x8};
    byte[] k1 = new byte[] {0xA,0x4,0x5,0x6,0x8,0x1,0x3,0x7,0xD,0xC,0xE,0x0,0x9,0x2,0xB,0xF};

    System.arraycopy(k8, 0, GostR3411_94_CryptoProParamSet.k8, 0, 16);
    System.arraycopy(k7, 0, GostR3411_94_CryptoProParamSet.k7, 0, 16);
    System.arraycopy(k6, 0, GostR3411_94_CryptoProParamSet.k6, 0, 16);
    System.arraycopy(k5, 0, GostR3411_94_CryptoProParamSet.k5, 0, 16);
    System.arraycopy(k4, 0, GostR3411_94_CryptoProParamSet.k4, 0, 16);
    System.arraycopy(k3, 0, GostR3411_94_CryptoProParamSet.k3, 0, 16);
    System.arraycopy(k2, 0, GostR3411_94_CryptoProParamSet.k2, 0, 16);
    System.arraycopy(k1, 0, GostR3411_94_CryptoProParamSet.k1, 0, 16);
  };

  /* Cipher context includes key and preprocessed  substitution block */
  private final static class GostCtx {
    final int k[] = new int[8];
    /* Constant s-boxes -- set up in gost_init(). */
    final int k87[] = new int[256];
    final int k65[] = new int[256];
    final int k43[] = new int[256];
    final int k21[] = new int[256];

    void init(GostSubstBlock b) {
      for (int i = 0; i < 256; i++) {
        k87[i] = (b.k8[i>>>4] <<4 | b.k7 [i &15])<<24;
        k65[i] = (b.k6[i>>>4] << 4 | b.k5 [i &15])<<16;
        k43[i] = (b.k4[i>>>4] <<4  | b.k3 [i &15])<<8;
        k21[i] = b.k2[i>>>4] <<4  | b.k1 [i &15];
      }
    }

    /* Part of GOST 28147 algorithm moved into separate function */
    final int f(int n, int x)  {
      long tmp = int2ulong(n) + int2ulong(x);
      if ( tmp >= TOP_UINT )
        tmp -= TOP_UINT;
      x = k87[ ((int) (tmp>>>24)) & 255] |
          k65[ ((int) (tmp>>>16)) & 255] |
          k43[ ((int) (tmp>>>8)) & 255] |
          k21[ ((int)tmp) & 255];
      /* Rotate left 11 bits */
      return ((int)(int2ulong(x)<<11)) | x>>>(32-11);
	}

    /* Low-level encryption routine - encrypts one 64 bit block*/
    final void gostcrypt(byte[] in, int inpos, byte[] out, int outpos) {
      int n1, n2; /* As named in the GOST */
      n1 = byte2uint(in[inpos+0]) |
          (byte2uint(in[inpos+1])<<8) |
          (byte2uint(in[inpos+2])<<16) |
          (byte2uint(in[inpos+3])<<24);
      n2 = byte2uint(in[inpos+4]) |
          (byte2uint(in[inpos+5])<<8) |
          (byte2uint(in[inpos+6])<<16) |
          (byte2uint(in[inpos+7])<<24);
      /* Instead of swapping halves, swap names each round */

      n2 ^= f(n1,k[0]); n1 ^= f(n2,k[1]);
      n2 ^= f(n1,k[2]); n1 ^= f(n2,k[3]);
      n2 ^= f(n1,k[4]); n1 ^= f(n2,k[5]);
      n2 ^= f(n1,k[6]); n1 ^= f(n2,k[7]);

      n2 ^= f(n1,k[0]); n1 ^= f(n2,k[1]);
      n2 ^= f(n1,k[2]); n1 ^= f(n2,k[3]);
      n2 ^= f(n1,k[4]); n1 ^= f(n2,k[5]);
      n2 ^= f(n1,k[6]); n1 ^= f(n2,k[7]);

      n2 ^= f(n1,k[0]); n1 ^= f(n2,k[1]);
      n2 ^= f(n1,k[2]); n1 ^= f(n2,k[3]);
      n2 ^= f(n1,k[4]); n1 ^= f(n2,k[5]);
      n2 ^= f(n1,k[6]); n1 ^= f(n2,k[7]);

      n2 ^= f(n1,k[7]); n1 ^= f(n2,k[6]);
      n2 ^= f(n1,k[5]); n1 ^= f(n2,k[4]);
      n2 ^= f(n1,k[3]); n1 ^= f(n2,k[2]);
      n2 ^= f(n1,k[1]); n1 ^= f(n2,k[0]);

      out[outpos+0] = (byte)(n2 & 0xff);
      out[outpos+1] = (byte)((n2>>>8) & 0xff);
      out[outpos+2] = (byte)((n2>>>16) & 0xff);
      out[outpos+3] = (byte)(n2>>>24);
      out[outpos+4] = (byte)(n1 & 0xff);
      out[outpos+5] = (byte)((n1>>>8) & 0xff);
      out[outpos+6] = (byte)((n1>>>16) & 0xff);
      out[outpos+7] = (byte)(n1>>>24);
    }

    /* Set 256 bit  key into context */
    final void gostSetKey(byte[] xk) {
      int i,j;
      for(i=0,j=0;i<8;i++,j+=4) {
        k[i] = byte2uint(xk[j]) |
                ( byte2uint(xk[j+1]) << 8 ) |
                ( byte2uint(xk[j+2]) << 16 ) |
                ( byte2uint(xk[j+3]) << 24 );
      }
    }

    /* Encrypts one block using specified key */
    final void gostEncrypt(byte[] key, byte[] inblock, int inpos, byte[] outblock, int outpos) {
      gostSetKey(key);
      gostcrypt(inblock, inpos, outblock, outpos);
    }
  }

  private final static class GostHashCtx {
    long len = 0;
    GostCtx cipher_ctx = null;
    int left = 0;
    final byte H[] = new byte[32];
    final byte S[] = new byte[32];
    final byte remainder[] = new byte[32];

    /**
     * Initialize gost_hash ctx - cleans up temporary structures and
     * set up substitution blocks
     */
    void init(GostSubstBlock subst_block) {
      len = 0;
      cipher_ctx = null;
      left = 0;
      java.util.Arrays.fill(H, (byte)0);
      java.util.Arrays.fill(S, (byte)0);
      java.util.Arrays.fill(remainder, (byte)0);

      cipher_ctx = new GostCtx();
      cipher_ctx.init(subst_block);
    }

    /**
     * reset state of hash context to begin hashing new message
     */
    final void startHash() {
      java.util.Arrays.fill(H, (byte)0);
      java.util.Arrays.fill(S, (byte)0);
      len = 0L;
      left = 0;
    }

    /**
     * Hash block of arbitrary length
     */
    final void hashBlock(byte[] block, int pos, int length) {
      final int lastPos = pos + length;
      if ( left > 0 ) {
        /* There are some bytes from previous step */
        int addBytes = 32 - left;
        if ( addBytes > length )
          addBytes = length;
        System.arraycopy(block, pos, remainder, left, addBytes);
        left += addBytes;
        if ( left < 32 )
          return;
        pos += addBytes;
        hashStep(H, remainder, 0);
        addBlocks(32, S, remainder, 0);
        len += 32;
        left = 0;
      }

      while ( lastPos-pos >= 32 ) {
        hashStep(H, block, pos);
        addBlocks(32, S, block, pos);
        len += 32;
        pos += 32;
      }

      if ( pos!=length ) {
        left = lastPos - pos;
        System.arraycopy(block, pos, remainder, 0, left);
      }
    }

    final void hashBlock(byte[] data, int len) {
      hashBlock(data, 0, len);
    }
    final void hashBlock(byte[] data) {
      hashBlock(data, 0, data.length);
    }

    /**
     * Compute hash value from current state of ctx
     * state of hash ctx becomes invalid and cannot be used for further
     * hashing.
     */
    final byte[] finishHash() {
      final byte[] buf = new byte[32];
      final byte[] xH = new byte[32];
      final byte[] xS = new byte[32];
      long fin_len = len;

      System.arraycopy(H, 0, xH, 0, 32);
      System.arraycopy(S, 0, xS, 0, 32);
      java.util.Arrays.fill(buf, (byte)0);

      if ( left > 0 ) {
        System.arraycopy(remainder, 0, buf, 0, left);
        hashStep(xH, buf, 0);
        addBlocks(32, xS, buf, 0);
        fin_len += left;
        java.util.Arrays.fill(buf, (byte)0);
      }

      fin_len<<=3; /* Hash length in BITS!!*/
      int bptr = 0;
      while ( fin_len > 0 ) {
        buf[bptr++] = (byte)(fin_len&0xFF);
        fin_len >>>= 8;
      }

      hashStep(xH, buf, 0);
      hashStep(xH, xS, 0);
      return xH;
    }

    final void hashStep(byte[] xH, byte[] xM, int mstart) {
      final byte[] xU = new byte[32];
      final byte[] xW = new byte[32];
      final byte[] xV = new byte[32];
      final byte[] xS = new byte[32];
      final byte[] Key = new byte[32];
      int i;
      /* Compute first key */
      xor_blocks(xW, xH, xM, mstart, 32);
      swap_bytes(xW, Key);
      /* Encrypt first 8 bytes of H with first key*/
      cipher_ctx.gostEncrypt(Key, xH, 0, xS, 0);
      /* Compute second key*/
      circle_xor8(xH, xU);
      circle_xor8(xM, mstart, xV);
      circle_xor8(xV, xV);
      xor_blocks(xW, xU, xV, 0, 32);
      swap_bytes(xW, Key);
      /* encrypt second 8 bytes of H with second key*/
      cipher_ctx.gostEncrypt(Key, xH, 8, xS, 8);
      /* compute third key */
      circle_xor8(xU, xU);

      xU[31]=(byte) ~xU[31];
      xU[29]=(byte) ~xU[29];
      xU[28]=(byte) ~xU[28];
      xU[24]=(byte) ~xU[24];
      xU[23]=(byte) ~xU[23];
      xU[20]=(byte) ~xU[20];
      xU[18]=(byte) ~xU[18];
      xU[17]=(byte) ~xU[17];
      xU[14]=(byte) ~xU[14];
      xU[12]=(byte) ~xU[12];
      xU[10]=(byte) ~xU[10];
      xU[ 8]=(byte) ~xU[ 8];
      xU[ 7]=(byte) ~xU[ 7];
      xU[ 5]=(byte) ~xU[ 5];
      xU[ 3]=(byte) ~xU[ 3];
      xU[ 1]=(byte) ~xU[ 1];

      circle_xor8(xV, xV);
      circle_xor8(xV, xV);
      xor_blocks(xW, xU, xV, 0, 32);
      swap_bytes(xW,Key);
      /* encrypt third 8 bytes of H with third key*/
      cipher_ctx.gostEncrypt(Key, xH, 16, xS, 16);
      /* Compute fourth key */
      circle_xor8(xU, xU);
      circle_xor8(xV, xV);
      circle_xor8(xV, xV);
      xor_blocks(xW, xU, xV, 0, 32);
      swap_bytes(xW,Key);
      /* Encrypt last 8 bytes with fourth key */
      cipher_ctx.gostEncrypt(Key, xH, 24, xS, 24);
      for (i=0;i<12;i++)
          transform_3(xS);
      xor_blocks(xS, xS, xM, mstart, 32);
      transform_3(xS);
      xor_blocks(xS, xS, xH, 0, 32);
      for (i=0;i<61;i++)
          transform_3(xS);
      System.arraycopy(xS, 0, xH, 0, 32);
    }
  }

  //----------------------------------------------------------------------------------

  private GostHashCtx ctx = null;

  public boolean isNative() {
    return false;
  }

  public void init() {
    final GostHashCtx tmp = new GostHashCtx();
    tmp.init(GostR3411_94_CryptoProParamSet);
    ctx = tmp;
  }

  public void done() {
    ctx = null;
  }

  public void startHash() {
    ctx.startHash();
  }

  public void hashBlock(byte[] block, int pos, int length) {
    if ( length < 0 && block != null )
      length = block.length;
    ctx.hashBlock(block, pos, length);
  }

  public byte[] finishHash() {
    return ctx.finishHash();
  }

  public byte[] calcHash(File file) {
    try {
      final FileInputStream fis = new FileInputStream(file);
      try {
        final byte[] buf = new byte[1024];
        ctx.startHash();
        while ( true ) {
          final int len = fis.read(buf);
          if ( len < 1 )
            break;
          ctx.hashBlock(buf, 0, len);
        }
        return ctx.finishHash();
      } finally {
        fis.close();
      }
    } catch(IOException x) {
      return null;
    }
  }

}
