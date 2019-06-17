package com.cqut.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/*
 给定一个包含 n 个整数的数组 arr，判断 arr 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？找出所有满足条件且不重复的三元组。

注意：答案中不可以包含重复的三元组。

例如, 给定数组 arr = [-1, 0, 1, 2, -1, -4]，

满足要求的三元组集合为：
[
  [-1, 0, 1],
  [-1, -1, 2]
]
 */
public class Test {
	private final int THRESHOLD = 1000;
	/**
	 * 在指定的数组中，获取指定个数的元素且这些元素的和为零
	 * @param arr 数组
	 * @param target 指定的个数
	 * @return
	 */
	public List<List<Integer>> targetNode(int[] arr, int target) {
		//1.判断
		if(arr.length < target) {
			return null;
		}
	    //2.排序
		if(arr.length < THRESHOLD)
			Arrays.sort(arr);
		else
			Arrays.parallelSort(arr);
		LinkedHashMap<Integer, Integer> map = storeMap(arr);
		//3.获取结果集
		List<List<Integer>> result = getResult(map, target - 1);
		if(result.size() == 0) {
			return null;
		}
		return result;
    }
	
	//获取结果集
	private List<List<Integer>> getResult(LinkedHashMap<Integer, Integer> map, int target) {
		List<List<Integer>> collection = new ArrayList<List<Integer>>();
		List<Node> sumList = getSumList(map, target);
		for(int i = 0; i < sumList.size(); i++) {
			if(sumList.get(i).val == 0) {
				sumList.get(i).list.sort(new Comparator<Integer>() {
					@Override
					public int compare(Integer num1, Integer num2) {
						return num1 - num2;
					}
				});
				collection.add(sumList.get(i).list);
			}
		}
		
		doDistinct(collection);
		
		return collection;
	}
	//去除集合中重复的元素,到达一定程度（size > THRESHOLD）会开启多线程处理
	private void doDistinct(List<List<Integer>> collection) {
		ForkJoinPool pool = new ForkJoinPool();
		ForkJoinTask<Long> result = pool.submit(new DistinctTask(collection, 0, collection.size()));
		
		try {
			result.get();//等待任务执行完成
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	//将map中所有元素的和储存在list中
	private List<Node> getSumList(LinkedHashMap<Integer, Integer> map, int target) {
		List<Node> augends = new ArrayList<Node>();//储存每个被加数
		for(int key : map.keySet()) {
			augends.add(storeNode(null, key));
		}
		
		for(int i = 0; i < target; i++) {
			int size = augends.size();
			for(int j = 0; j < size; j++) {
				Node augend = augends.remove(0);
				for(int key : map.keySet()) {
					
					//防止重复相加,因为是有序数组，所以每次相加应该至少从等于自己的那个数开始
					if(key < augend.list.get(0)) {
						continue;
					}
					
					/**
					 * 防止重复遍历
					 * 	1.如果augend list集合中已有该元素，则需要判断该元素的个数与当前大遍历次数的
					 * 	关系，只有大于当前大遍历次数时才能进行遍历
					 *  2.如果augend list集合中没有该元素，则可直接遍历
					 */
					if(augend.list.contains(key) && map.get(key) > i + 1) {
						augends.add(storeNode(augend.list,key));
					}else if(!augend.list.contains(key)){
						augends.add(storeNode(augend.list,key));
					}
				}
			}
		}
	    return augends;
	}

	//将有序数组储存在LinkedHashMap中
	private LinkedHashMap<Integer,Integer> storeMap(int[] arr) {
		LinkedHashMap<Integer,Integer> map = new LinkedHashMap<Integer,Integer>();
		for(int i = 0; i < arr.length; i++) {
			int val = 1;
			if(map.containsKey(arr[i])) {
				val = map.get(arr[i]);
				val++;
			}
			map.put(arr[i], val);
		}
		return map;
	}

	//将因子与和封装到一个node里
	private Node storeNode(List<Integer> list,int... nums) {
		Node node = new Node();
		List<Integer> tempList = new ArrayList<Integer>();
		int sum = 0;
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				sum += list.get(i);
				tempList.add(list.get(i));
			}
		}
		for(int j = 0; j < nums.length; j++) {
			sum+=nums[j];
			tempList.add(nums[j]);
		}
		node.val = sum;
		node.list = tempList;
		return node;
	}
	
    public static void main(String[] args){
        int[] arr = {-1, 0, 1, 2, -1, -4, 0, 0};
        Test t = new Test();
        //获取指定个数元素相加为零的数的集合
        List<List<Integer>> list = t.targetNode(arr,3);
        if(list == null) {
        	System.out.println("无满足条件的数据");
        }else {
        	list.parallelStream().forEach(System.out::print);
        }
    }
    
    private class Node{
    	int val;// 因子的和
    	List<Integer> list;//加数因子集合
    }
    
    private class DistinctTask extends RecursiveTask<Long>{
    	private List<List<Integer>> collection;
    	private long start;
    	private long end;
		public DistinctTask(List<List<Integer>> collection, long start, long end) {
			this.collection = collection;
			this.start = start;
			this.end = end;
		}


		@Override
		protected Long compute() {
			HashSet<List> set = new HashSet<>();
			if((end - start) > THRESHOLD) {
				long mid = (start + end) >> 1;
				DistinctTask task1 = new DistinctTask(collection, start, mid);
				DistinctTask task2 = new DistinctTask(collection, mid + 1, end);
				
				task1.fork();
				task2.fork();
				
				end = task1.join() - task2.join() + mid;
			}
			for(long i = start; i <= end; i++ ) {
				if(!set.add(collection.get((int) i))) {
					collection.remove(collection.get((int) i));
					end --;
				}
			}
			return end;
		}
    }
}
