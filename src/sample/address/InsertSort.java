package sample.address;

public class InsertSort {
	static void Sort(int[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int j = i;
			while (j > 0 && a[j] > a[j - 1]) {
				int temp = a[j];
				a[j] = a[j - 1];
				a[j - 1] = temp;
				j--;
			}
		}
	}
}
