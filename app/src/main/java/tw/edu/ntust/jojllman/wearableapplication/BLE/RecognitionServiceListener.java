package tw.edu.ntust.jojllman.wearableapplication.BLE;

public interface RecognitionServiceListener
{
	void onRecognized(String word);
	
	void onRecognizeFinished();
}
