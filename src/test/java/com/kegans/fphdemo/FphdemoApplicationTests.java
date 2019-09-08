package com.kegans.fphdemo;

import org.assertj.core.api.BigIntegerAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FphdemoApplicationTests {



	@Test
	public void contextLoads() {
	}

	@Test
	public void testCCNumber() {
		try {
			FieldPreservingHash fph = new FieldPreservingHash("1234 5678 9123 1234", "SHA-256");
			assertThat(fph.hash()).isEqualTo("9610 5612 1374 9443");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testNumberSpacePunctuationPreserve() {
		try {
			FieldPreservingHash fph = new FieldPreservingHash("21 baker street, london", "SHA-256");
			assertThat(fph.hash()).isEqualTo("02 KCvgr uOTqaV, krxbEu");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPerformanceAndCollisions() throws IOException {
		try {

			List<String> outputs = new ArrayList<>();

			BigInteger max = new BigInteger("9999");

			System.out.println(BigInteger.valueOf(0).compareTo(max));
			System.out.println(max.compareTo(max));

			for (BigInteger i = BigInteger.valueOf(0); i.compareTo(max) <= 0; i=i.add(BigInteger.ONE)) {
				FieldPreservingHash fph = new FieldPreservingHash(i.toString(), "SHA-256");
				outputs.add(fph.hash());
			}

			System.out.println("done");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
