package com.theultimatelabs.sonic;

import android.util.Log;

public class log {
	private static String makeTag() {
		StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
		String className = caller.getFileName().substring(0, caller.getFileName().length()-5);
		return String.format("%s:%d %s",className,caller.getLineNumber(),caller.getMethodName()) ;
	}
	
	public static void v(String tag, String format, Object...args)
    {
       Log.v(tag, String.format(format, args));
    }
	public static void v(String format, Object...args)
    {
		v(makeTag(),format,args);
    }
	public static void v(String format)
    {
		v(makeTag(),format);
    }
	public static void i(String tag, String format, Object...args)
    {
       Log.i(tag, String.format(format, args));
    }
	public static void i(String format, Object...args)
    {
		i(makeTag(),format,args);
    }
	public static void i(String format)
    {
		i(makeTag(),format);
    }
	public static void w(String tag, String format, Object...args)
    {
       Log.w(tag, String.format(format, args));
    }
	public static void w(String format, Object...args)
    {
		w(makeTag(),format,args);
    }
	public static void w(String format)
    {
		w(makeTag(),format);
    }
	public static void e(String tag, String format, Object...args)
    {
       Log.e(tag, String.format(format, args));
    }
	public static void e(String format, Object...args)
    {
		e(makeTag(),format,args);
    }
	public static void e(String format)
    {
		e(makeTag(),format);
    }
	public static void d(String tag, String format, Object...args)
    {
       Log.d(tag, String.format(format, args));
    }
	public static void d(String format, Object...args)
    {
		d(makeTag(),format,args);
    }
	public static void d(String format)
    {
		d(makeTag(),format);
    }
}
