package com.littlecat.powerbank.queue;

import java.util.LinkedList;
public class MyQueue
{
  private LinkedList list = new LinkedList();
  public void clear()
  {
      list.clear();
  }
  public boolean QueueEmpty()
  {
      return list.isEmpty();
  }
  public void enQueue(Object o)//进队
  {
      list.addLast(o);
  }
  public Object deQueue()//出队
  {
      if(!list.isEmpty())
      {
          return list.removeFirst();
      }
      return null;
  }
  public int QueueLength()//获取队列长度
  {
      return list.size();
  }
  public Object QueuePeek()//查看队首元素
  {
      return list.getFirst();
  }

}