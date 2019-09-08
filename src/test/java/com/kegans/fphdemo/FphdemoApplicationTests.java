package com.kegans.fphdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
			FieldPreservingHash fph = new FieldPreservingHash("1234 5678 9123 1234");
			assertThat(fph.hash()).isEqualTo("3779 2869 0587 5929");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testNumberSpacePunctuationPreserve() {
		try {
			FieldPreservingHash fph = new FieldPreservingHash("21 baker street, london");
			assertThat(fph.hash()).isEqualTo("55 mLLPj CTzzLa, hHngFc");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
