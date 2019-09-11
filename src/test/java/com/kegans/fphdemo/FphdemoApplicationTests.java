package com.kegans.fphdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static com.kegans.fphdemo.FPHService.convertToHex;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FphdemoApplicationTests {


	public static String hashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength ) {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
			PBEKeySpec spec = new PBEKeySpec( password, salt, iterations, keyLength );
			SecretKey key = skf.generateSecret( spec );
			byte[] res = key.getEncoded( );
			return convertToHex(res);
		} catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
			throw new RuntimeException( e );
		}
	}



	static int smash(char[] input) {
		int hashCode = 0;
		for (char c : input) {
			// rotate the bits
			hashCode = (hashCode << 1) | (hashCode >> (32 - 1));
			// xor new bits
			hashCode ^= c;
		}
		return hashCode;
	}


	String alphabet;

	@Test
	public void contextLoads() {
		alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
	}


	@Test
	public  void smearTest() {
		String hash = hashPassword("test1234".toCharArray(), "salt".getBytes(), 1000, 16);
		System.out.println(hash);
	}


	@Test
	public void testCCNumber() throws UnsupportedEncodingException {
		try {
			FieldPreservingHash fph = new FieldPreservingHash("1234 5678 9123 1234", ", ", "SHA-256", alphabet);
			assertThat(fph.hash()).isEqualTo("9610 5612 1374 9443");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testNumberSpacePunctuationPreserve() throws UnsupportedEncodingException {
		try {
			FieldPreservingHash fph = new FieldPreservingHash("21 baker street, london", ", ", "SHA-256", alphabet);
			assertThat(fph.hash()).isEqualTo("02 KCvgr uOTqaV, krxbEu");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPerformanceAndCollisions() throws UnsupportedEncodingException {
		try {

			List<String> outputs = new ArrayList<>();

			BigInteger max = new BigInteger("9999");

			System.out.println(BigInteger.valueOf(0).compareTo(max));
			System.out.println(max.compareTo(max));

			for (BigInteger i = BigInteger.valueOf(0); i.compareTo(max) <= 0; i=i.add(BigInteger.ONE)) {
				FieldPreservingHash fph = new FieldPreservingHash(i.toString(), ", ", "SHA-256", alphabet);
				outputs.add(fph.hash());
			}

			System.out.println("done");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
