package com.hawk.map.polygon.core;

/**
 * 画地块的历史记录对象，用来保存和恢复记录
 *
 * @param <T> 历史记录
 */
public interface IHistoryManager<T> {

	/**
	 * 将历史记录压栈
	 *
	 * @param history 历史记录
	 */
	void push(T history);

	/**
	 * 恢复历史记录栈顶的条目并将其弹出
	 */
	void pop();

	/**
	 * 当前是否没有历史记录
	 *
	 * @return true 没有历史记录，false 有。
	 */
	boolean isEmpty();

	/**
	 * 清空历史记录
	 */
	void clear();

	/**
	 * 销毁历史记录管理器
	 */
	void destroy();
}
