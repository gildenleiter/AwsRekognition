package com.daibutsu.common;

public class Const {
	// privateコンストラクタでインスタンス生成を抑止
	private Const(){
		
	}
	
	/***
	 * カスタムモデルの状態
	 */
	public static  final String STARTING = "STARTING";
	public static final String RUNNING = "RUNNING";
	public static final String FAILED = "FAILED";
	public static final String STOPPED = "STOPPED";
	public static final String DELETING = "DELETING";
	public static final String TRAINING_FAILED = "TRAINING_FAILED";
	public static final String TRAINING_COMPLETED = "TRAINING_COMPLETED";
	public static final String TRAINING_IN_PROGRESS = "TRAINING_IN_PROGRESS";
	
}
