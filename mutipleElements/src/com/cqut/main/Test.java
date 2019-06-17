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
 ����һ������ n ������������ arr���ж� arr ���Ƿ��������Ԫ�� a��b��c ��ʹ�� a + b + c = 0 ���ҳ��������������Ҳ��ظ�����Ԫ�顣

ע�⣺���в����԰����ظ�����Ԫ�顣

����, �������� arr = [-1, 0, 1, 2, -1, -4]��

����Ҫ�����Ԫ�鼯��Ϊ��
[
  [-1, 0, 1],
  [-1, -1, 2]
]
 */
public class Test {
	private final int THRESHOLD = 1000;
	/**
	 * ��ָ���������У���ȡָ��������Ԫ������ЩԪ�صĺ�Ϊ��
	 * @param arr ����
	 * @param target ָ���ĸ���
	 * @return
	 */
	public List<List<Integer>> targetNode(int[] arr, int target) {
		//1.�ж�
		if(arr.length < target) {
			return null;
		}
	    //2.����
		if(arr.length < THRESHOLD)
			Arrays.sort(arr);
		else
			Arrays.parallelSort(arr);
		LinkedHashMap<Integer, Integer> map = storeMap(arr);
		//3.��ȡ�����
		List<List<Integer>> result = getResult(map, target - 1);
		if(result.size() == 0) {
			return null;
		}
		return result;
    }
	
	//��ȡ�����
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
	//ȥ���������ظ���Ԫ��,����һ���̶ȣ�size > THRESHOLD���Ὺ�����̴߳���
	private void doDistinct(List<List<Integer>> collection) {
		ForkJoinPool pool = new ForkJoinPool();
		ForkJoinTask<Long> result = pool.submit(new DistinctTask(collection, 0, collection.size()));
		
		try {
			result.get();//�ȴ�����ִ�����
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	//��map������Ԫ�صĺʹ�����list��
	private List<Node> getSumList(LinkedHashMap<Integer, Integer> map, int target) {
		List<Node> augends = new ArrayList<Node>();//����ÿ��������
		for(int key : map.keySet()) {
			augends.add(storeNode(null, key));
		}
		
		for(int i = 0; i < target; i++) {
			int size = augends.size();
			for(int j = 0; j < size; j++) {
				Node augend = augends.remove(0);
				for(int key : map.keySet()) {
					
					//��ֹ�ظ����,��Ϊ���������飬����ÿ�����Ӧ�����ٴӵ����Լ����Ǹ�����ʼ
					if(key < augend.list.get(0)) {
						continue;
					}
					
					/**
					 * ��ֹ�ظ�����
					 * 	1.���augend list���������и�Ԫ�أ�����Ҫ�жϸ�Ԫ�صĸ����뵱ǰ�����������
					 * 	��ϵ��ֻ�д��ڵ�ǰ���������ʱ���ܽ��б���
					 *  2.���augend list������û�и�Ԫ�أ����ֱ�ӱ���
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

	//���������鴢����LinkedHashMap��
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

	//��������ͷ�װ��һ��node��
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
        //��ȡָ������Ԫ�����Ϊ������ļ���
        List<List<Integer>> list = t.targetNode(arr,3);
        if(list == null) {
        	System.out.println("����������������");
        }else {
        	list.parallelStream().forEach(System.out::print);
        }
    }
    
    private class Node{
    	int val;// ���ӵĺ�
    	List<Integer> list;//�������Ӽ���
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
