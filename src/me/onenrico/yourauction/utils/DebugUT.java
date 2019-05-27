package me.onenrico.yourauction.utils;

public class DebugUT {
	private long now;
	private long lasthit;

	public DebugUT() {
		now = System.currentTimeMillis();
		lasthit = now;
	}

	public void hit(String msg) {
		MessageUT.cmsg(msg + "> After: " + (lasthit - now) + "ms");
		lasthit = System.currentTimeMillis();
		MessageUT.cmsg(msg + "> Different: " + (System.currentTimeMillis() - now) + "ms");
	}
}
