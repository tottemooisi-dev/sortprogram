package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.webapp.repository.SortResultRepository;
import com.example.webapp.entity.SortResult;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class SortController {

    @Autowired
    private SortResultRepository sortResultRepository;

    // ソート種別を表す定数
    private static final String SORT_TYPE_BUBBLE = "bubble";
    private static final String SORT_TYPE_SELECTION = "selection";
    private static final String SORT_TYPE_INSERTION = "insertion";
    private static final String SORT_TYPE_MERGE = "merge";
    private static final String SORT_TYPE_QUICK = "quick";
    private static final String SORT_TYPE_BOGO = "bogo";
    private static final String SORT_TYPE_STALIN = "stalin";
    private static final String SORT_TYPE_WAVE = "wave";

    /**
     * ソート過程の1ステップを表現するクラス。
     */
    public static class SortStep {
        private final int[] array;              // 現在の配列状態
        private final int[] activeIndices;      // 現在操作中のインデックス
        private final int[] eliminatedIndices;  // スタリンソートで削除されたインデックス

        public SortStep(int[] array, int[] activeIndices, int[] eliminatedIndices) {
            this.array = Arrays.copyOf(array, array.length);
            this.activeIndices = activeIndices;
            this.eliminatedIndices = eliminatedIndices;
        }

        public int[] getArray() { return array; }
        public int[] getActiveIndices() { return activeIndices; }
        public int[] getEliminatedIndices() { return eliminatedIndices; }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/sort")
    public String sort(@RequestParam String sortType, @RequestParam String numbers, Model model) {
        model.addAttribute("selectedSortType", sortType);   // 選択されたソート種別を保持（開始ボタンを押してもそのままにするため）
        model.addAttribute("currentNumbers", numbers);      // 入力された数字を保持（開始ボタンを押してもそのままにするため）

        // 有効な数字が入力されていない場合のエラーチェック
        int[] array = parseNumbers(numbers);
        if (array == null || array.length == 0) {// array内がnullの場合or有効な数字が1つもない場合
            model.addAttribute("errorMessage", "有効な数字が入力されていません。");
            return "index"; // エラーメッセージを表示して終了
        }

        List<SortStep> sortSteps = new ArrayList<>(); // ソート過程のステップを格納するリスト

        // 選択されたソート種別に基づいてソートを実行
        switch (sortType) {
            case SORT_TYPE_BUBBLE    -> bubbleSort(array, sortSteps);
            case SORT_TYPE_SELECTION -> selectionSort(array, sortSteps);
            case SORT_TYPE_INSERTION -> insertionSort(array, sortSteps);
            case SORT_TYPE_MERGE     -> mergeSort(array, sortSteps);
            case SORT_TYPE_QUICK     -> quickSort(array, sortSteps);
            case SORT_TYPE_BOGO      -> bogoSort(array, sortSteps);
            case SORT_TYPE_STALIN    -> stalinSort(array, sortSteps);
            case SORT_TYPE_WAVE      -> waveAnimation(array, sortSteps);
            default -> { /* 何もしない */ }
        }

        model.addAttribute("sortSteps", sortSteps); // ソート過程のステップをモデルに保持

        // データベースにソート結果を保存
        SortResult result = new SortResult();
        result.setSortType(sortType);
        result.setOriginalNumbers(numbers);
        result.setSortedNumbers(Arrays.stream(array).mapToObj(String::valueOf).collect(Collectors.joining()));
        result.setExecutionTime(LocalDateTime.now());
        sortResultRepository.save(result);

        return "index"; // 結果表示はindex.htmlに返して行う
    }

    // 入力された数字の文字列を解析して整数配列に変換するヘルパーメソッド
    private int[] parseNumbers(String numbers) {
        if (numbers == null || numbers.trim().isEmpty()) return new int[0];// nullや空文字列の場合は空の配列を返す
        return numbers.chars().filter(Character::isDigit).map(c -> c - '0').toArray();// 数字以外の文字を無視して整数配列に変換
    }

    // ソートアルゴリズムステップ記録のヘルパーメソッド
    private SortStep newStep(int[] arr, int... active) {// 可変長引数で操作中のインデックスを受け取る
        return new SortStep(arr, active, new int[]{});  // 操作中のインデックスのみ設定し、削除されたインデックスは空配列にする
    }

    private void bubbleSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                steps.add(newStep(arr, j, j + 1));// 比較中の2つの要素をハイライト
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                    steps.add(newStep(arr, j, j + 1));// 交換後の状態を追加
                }
            }
        }
        steps.add(newStep(arr));
    }

    private void selectionSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                steps.add(newStep(arr, i, j, minIndex));// 比較中の2つの要素をハイライト
                if (arr[j] < arr[minIndex]) minIndex = j;
            }
            int temp = arr[minIndex]; arr[minIndex] = arr[i]; arr[i] = temp;
            steps.add(newStep(arr, i, minIndex));// 交換後の状態を追加
        }
        steps.add(newStep(arr));// 最終状態を追加
    }

    private void insertionSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i]; int j = i - 1;
            steps.add(newStep(arr, i, j));// 比較中の2つの要素をハイライト
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                steps.add(newStep(arr, j, j + 1));// 交換後の状態を追加
                j--;
                steps.add(newStep(arr, j, j + 1));// 次の比較中の2つの要素をハイライト
                j--;
            }
            arr[j + 1] = key;
            steps.add(newStep(arr, j + 1));// keyを挿入した後の状態を追加
        }
        steps.add(newStep(arr));// 最終状態を追加
    }

    private void mergeSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        mergeSortHelper(arr, 0, arr.length - 1, steps);
        steps.add(newStep(arr));// 最終状態を追加
    }

    private void mergeSortHelper(int[] arr, int left, int right, List<SortStep> steps) {
        if (left < right) {
            int middle = left + (right - left) / 2;
            mergeSortHelper(arr, left, middle, steps);
            mergeSortHelper(arr, middle + 1, right, steps);
            merge(arr, left, middle, right, steps);
        }
    }

    private void merge(int[] arr, int left, int middle, int right, List<SortStep> steps) {
        int[] leftArray = Arrays.copyOfRange(arr, left, middle + 1);
        int[] rightArray = Arrays.copyOfRange(arr, middle + 1, right + 1);
        int i = 0, j = 0, k = left;
        while (i < leftArray.length && j < rightArray.length) {
            steps.add(newStep(arr, left + i, middle + 1 + j));// 比較中の2つの要素をハイライト
            if (leftArray[i] <= rightArray[j]) arr[k++] = leftArray[i++];
            else arr[k++] = rightArray[j++];
            steps.add(newStep(arr));// 合併後の状態を追加
        }
        while (i < leftArray.length) { arr[k++] = leftArray[i++]; steps.add(newStep(arr)); }// 左部分配列の残りを追加
        while (j < rightArray.length) { arr[k++] = rightArray[j++]; steps.add(newStep(arr)); }// 右部分配列の残りを追加
    }

    private void quickSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        quickSortHelper(arr, 0, arr.length - 1, steps);
        steps.add(newStep(arr));// 最終状態を追加
    }

    private void quickSortHelper(int[] arr, int low, int high, List<SortStep> steps) {
        if (low < high) {
            int pi = partition(arr, low, high, steps);
            quickSortHelper(arr, low, pi - 1, steps);
            quickSortHelper(arr, pi + 1, high, steps);
        }
    }

    private int partition(int[] arr, int low, int high, List<SortStep> steps) {
        int pivot = arr[high]; int i = (low - 1);
        for (int j = low; j < high; j++) {
            steps.add(newStep(arr, j, high));// ピボットと比較中の要素をハイライト
            if (arr[j] < pivot) {
                i++; int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
                steps.add(newStep(arr, i, j));// 交換後の状態を追加
            }
        }
        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        steps.add(newStep(arr, i + 1, high));// 最終状態を追加
        return i + 1;
    }

    private void bogoSort(int[] arr, List<SortStep> steps) {
        steps.add(newStep(arr));// 初期状態を追加
        while (!isSorted(arr)) {
            shuffle(arr);
            int[] allIndices = new int[arr.length];
            for(int i=0; i<arr.length; i++) allIndices[i] = i;
            steps.add(newStep(arr, allIndices));// シャッフル前の状態を追加
        }
        steps.add(newStep(arr));// 最終状態を追加
    }

    private void stalinSort(int[] arr, List<SortStep> steps) {
        if (arr.length == 0) {
            steps.add(newStep(arr));// 空配列の場合は初期状態のみ追加して終了
            return;
        }
        steps.add(newStep(arr));
        List<Integer> eliminated = new ArrayList<>();
        int max_val = arr[0];
        int max_idx = 0;

        for (int i = 1; i < arr.length; i++) {
            // 現在の要素と最大値のインデックスをハイライト
            steps.add(new SortStep(arr, new int[]{i, max_idx}, eliminated.stream().mapToInt(Integer::intValue).toArray()));
            if (arr[i] < max_val) {
                eliminated.add(i);// 現在の要素を削除リストに追加
            } else {
                max_val = arr[i];
                max_idx = i;
            }
        }
        // 最終状態を追加
        steps.add(new SortStep(arr, new int[]{}, eliminated.stream().mapToInt(Integer::intValue).toArray()));
    }

    private void waveAnimation(int[] arr, List<SortStep> steps) {
        int n = arr.length;
        if (n == 0) return;

        steps.add(newStep(arr)); // 初期状態

        // 5周分、配列を左に循環シフトさせる
        for (int i = 0; i < n * 5; i++) {
            // 配列を1つ左にシフト
            int first = arr[0];
            System.arraycopy(arr, 1, arr, 0, n - 1);// 配列を左にシフト
            arr[n - 1] = first;

            // 末尾に移動した要素をハイライト
            steps.add(newStep(arr, n - 1));
        }

        steps.add(newStep(arr)); // 最終状態
    }

    private boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) if (arr[i] > arr[i + 1]) return false;// 昇順にソートされていない場合はfalseを返す
        return true;
    }

    private void shuffle(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));// シャッフル前の状態を追加
            int temp = arr[index]; arr[index] = arr[i]; arr[i] = temp;// 要素を交換
        }
    }
}
