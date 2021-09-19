package javatuning.ch3.ref;

public class MyObject {
	// 被gc线程调用
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("MyObject's finalize called thread name:" + Thread.currentThread().getName());
	}
	@Override
	public String toString(){
		return "I am MyObject";
	}
}
