package com.pluseq.coreader;

import java.util.ArrayList;
import com.pluseq.coreader.CoBit;

public class CoBitsQueue extends ArrayList<CoBit> implements java.util.Queue<CoBit> {

	private static final long serialVersionUID = 1L;

	@Override
	public CoBit element() {
		return peek();
	}

	@Override
	public boolean offer(CoBit o) {
		add(o);
		return true;
	}

	@Override
	public CoBit peek() {
		return get(0);
	}

	@Override
	public CoBit poll() {
		return remove();
	}

	@Override
	public CoBit remove() {
		CoBit temp = peek();
		super.remove(0);
		return temp;
	}
}