package pl.shareddrawboard.api;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class ConnectorTest {

	public void setup() {
		PowerMockito.mockStatic(Log.class);
		//PowerMockito.wh
	}

	@Test
	public void testSendBoardUpdate() throws Exception {
		String file = "testSendBoardUpdate.txt";

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		FileInputStream fileInputStream = new FileInputStream(file);

		Connector connector = new Connector("abc", 12345, "qwerty", null, false);

		String expectedTestString = "dfckldfjvklgjvkrgvjkrgjvkrgfjvkgj";
		connector.writeString(fileOutputStream, expectedTestString);

		String readTestString = connector.readString(fileInputStream);

		Assert.assertEquals(expectedTestString, readTestString);
	}
}