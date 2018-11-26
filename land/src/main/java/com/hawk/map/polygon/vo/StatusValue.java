package com.hawk.map.polygon.vo;

public class StatusValue<T> {
	public int status;
	public T value;

	public StatusValue(int status, T value) {
		this.status = status;
		this.value = value;
	}
}
