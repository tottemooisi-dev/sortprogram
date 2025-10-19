document.addEventListener('DOMContentLoaded', function () {
    // DOM要素の取得
    const sortTypeSelect = document.getElementById('sortType');
    const numbersInput = document.getElementById('numbers');
    const generateRandomBtn = document.getElementById('generate-random');
    const sortStartBtn = document.getElementById('sort-start');
    const stopAnimationBtn = document.getElementById('stop-animation');
    const visualizationArea = document.getElementById('visualization-area');
    const descriptionArea = document.getElementById('sort-description');

    // GSAPのFlipプラグインを登録
    gsap.registerPlugin(Flip);

    // ソートの説明
    const sortDescriptions = {
        bubble: 'バブルソート　隣り合う要素を比較し、大小関係が逆であれば交換を繰り返すことでソートします。最も単純なアルゴリズムの一つです。',
        selection: '選択ソート　未ソートの部分から最小値（または最大値）を見つけ、それをソート済みの部分の末尾に追加していく方法です。',
        insertion: '挿入ソート　ソート済みの部分に、未ソートの部分から要素を一つずつ取り出して適切な位置に挿入していく方法です。',
        merge: 'マージソート　データが単独になるまで２つずつ分割し、それぞれをソートしてから統合（マージ）することで、全体のソートを行うアルゴリズムです。',
        quick: 'クイックソート　ある基準値（ピボット）を選び、それより小さい要素と大きい要素に分割し、再帰的にソートしていく方法です。',
        bogo: 'ボゴソート　ひたすらランダムに並べ替え、それがソート済みになるまで繰り返す、天に身を任せるアルゴリズムです。',
        stalin: 'スターリンソート　配列の先頭から順に見ていき、前の要素より後の要素が小さければ「粛清」していく方法です。',
        wave: 'ウェーブ　これはソートではありません。隣り合う要素が「大、小、大、小...」という波のような順序になるように動かすアルゴリズムです。'
    };

    let animationFrame = 0;
    let animationTimer = null; // setTimeoutのIDまたはGSAPのタイムラインインスタンスを保持
    let sortSteps = [];
    let bogoRotationTween = null; // GSAPのTweenインスタンスを保持

    // =================================
    // 描画 & アニメーション関連
    // =================================

    function drawBars(array, activeIndices = [], eliminatedIndices = []) {
        visualizationArea.innerHTML = '';
        const maxVal = Math.max(...array, 1);

        array.forEach((val, i) => {
            const bar = document.createElement('div');
            bar.className = 'bar';
            bar.style.height = (val / maxVal * 90) + '%';
            bar.textContent = val;

            if (eliminatedIndices.includes(i)) {
                bar.classList.add('eliminated');
            } else if (activeIndices.includes(i)) {
                bar.classList.add('active');
            }
            
            visualizationArea.appendChild(bar);
        });
    }

    // 通常のソートアルゴリズム用のステップ再生アニメーション
    function animateSort() {
        if (animationTimer) clearTimeout(animationTimer);
        
        if (animationFrame < sortSteps.length) {
            const currentStep = sortSteps[animationFrame];
            drawBars(currentStep.array, currentStep.activeIndices, currentStep.eliminatedIndices);
            animationFrame++;

            let delay = 300;
            if (sortTypeSelect.value === 'bogo' || sortTypeSelect.value === 'wave') delay = 50;
            
            animationTimer = setTimeout(animateSort, delay);
        } else {
            if (bogoRotationTween) {
                bogoRotationTween.kill();
                gsap.to(visualizationArea, { rotation: 0, duration: 0.5 });
            }
            if (waveMotionTween) {
                waveMotionTween.kill();
                gsap.to(visualizationArea, { x: 0, duration: 0.5 });
            }
            visualizationArea.classList.remove('waving');
        }
    }

    function updateSortDescription() {
        const selectedValue = sortTypeSelect.value;
        descriptionArea.textContent = sortDescriptions[selectedValue] || '説明が見つかりません。';
        gsap.fromTo(descriptionArea, { opacity: 0 }, { opacity: 1, duration: 0.4 });
    }

    // =================================
    // ソートアルゴリズム
    // =================================
    function newStep(arr, active = [], eliminated = []) { return { array: [...arr], activeIndices: active, eliminatedIndices: eliminated }; }
    function bubbleSort(arr, steps) { steps.push(newStep(arr)); let n = arr.length; for (let i = 0; i < n - 1; i++) { for (let j = 0; j < n - i - 1; j++) { steps.push(newStep(arr, [j, j + 1])); if (arr[j] > arr[j + 1]) { [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]]; steps.push(newStep(arr, [j, j + 1])); } } } steps.push(newStep(arr)); }
    function selectionSort(arr, steps) { steps.push(newStep(arr)); let n = arr.length; for (let i = 0; i < n - 1; i++) { let minIndex = i; for (let j = i + 1; j < n; j++) { steps.push(newStep(arr, [i, j, minIndex])); if (arr[j] < arr[minIndex]) minIndex = j; } [arr[minIndex], arr[i]] = [arr[i], arr[minIndex]]; steps.push(newStep(arr, [i, minIndex])); } steps.push(newStep(arr)); }
    function insertionSort(arr, steps) { steps.push(newStep(arr)); let n = arr.length; for (let i = 1; i < n; i++) { let key = arr[i]; let j = i - 1; steps.push(newStep(arr, [i, j])); while (j >= 0 && arr[j] > key) { arr[j + 1] = arr[j]; steps.push(newStep(arr, [j, j + 1])); j--; } arr[j + 1] = key; steps.push(newStep(arr, [j + 1])); } steps.push(newStep(arr)); }
    function mergeSort(arr, steps) { steps.push(newStep(arr)); mergeSortHelper(arr, 0, arr.length - 1, steps); steps.push(newStep(arr)); }
    function mergeSortHelper(arr, left, right, steps) { if (left < right) { const middle = Math.floor(left + (right - left) / 2); mergeSortHelper(arr, left, middle, steps); mergeSortHelper(arr, middle + 1, right, steps); merge(arr, left, middle, right, steps); } }
    function merge(arr, left, middle, right, steps) { const leftArray = arr.slice(left, middle + 1); const rightArray = arr.slice(middle + 1, right + 1); let i = 0, j = 0, k = left; while (i < leftArray.length && j < rightArray.length) { steps.push(newStep(arr, [left + i, middle + 1 + j])); if (leftArray[i] <= rightArray[j]) arr[k++] = leftArray[i++]; else arr[k++] = rightArray[j++]; steps.push(newStep(arr)); } while (i < leftArray.length) { arr[k++] = leftArray[i++]; steps.push(newStep(arr)); } while (j < rightArray.length) { arr[k++] = rightArray[j++]; steps.push(newStep(arr)); } }
    function quickSort(arr, steps) { steps.push(newStep(arr)); quickSortHelper(arr, 0, arr.length - 1, steps); steps.push(newStep(arr)); }
    function quickSortHelper(arr, low, high, steps) { if (low < high) { let pi = partition(arr, low, high, steps); quickSortHelper(arr, low, pi - 1, steps); quickSortHelper(arr, pi + 1, high, steps); } }
    function partition(arr, low, high, steps) { let pivot = arr[high]; let i = low - 1; for (let j = low; j < high; j++) { steps.push(newStep(arr, [j, high])); if (arr[j] < pivot) { i++; [arr[i], arr[j]] = [arr[j], arr[i]]; steps.push(newStep(arr, [i, j])); } } [arr[i + 1], arr[high]] = [arr[high], arr[i + 1]]; steps.push(newStep(arr, [i + 1, high])); return i + 1; }
    function bogoSort(arr, steps) { steps.push(newStep(arr)); const isSorted = (a) => { for (let i = 0; i < a.length - 1; i++) if (a[i] > a[i + 1]) return false; return true; }; const shuffle = (a) => { for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } }; while (!isSorted(arr)) { shuffle(arr); steps.push(newStep(arr, arr.map((_, i) => i))); } steps.push(newStep(arr)); }
    function stalinSort(arr, steps) {
        if (arr.length === 0) {
            steps.push(newStep(arr));
            return;
        }
        steps.push(newStep(arr)); // 初期状態
        let maxVal = arr[0];
        let maxIdx = 0;
        const eliminated = [];
        for (let i = 1; i < arr.length; i++) {
            steps.push(newStep(arr, [i, maxIdx], [...eliminated])); // 比較のステップ
            if (arr[i] < maxVal) {
                eliminated.push(i); // 粛清対象を追加
                steps.push(newStep(arr, [i, maxIdx], [...eliminated])); // 粛清が決定したステップ
            } else {
                maxVal = arr[i];
                maxIdx = i;
            }
        }
        steps.push(newStep(arr, [], eliminated)); // 最終結果のステップ
    }
    function waveSort(arr, steps) {
        const n = arr.length;
        if (n === 0) return;

        steps.push(newStep(arr)); // 初期状態

        // 5周分、配列を左に循環シフトさせる
        for (let i = 0; i < n * 5; i++) {
            const first = arr.shift(); // 先頭の要素を取り出す
            arr.push(first);           // 末尾に追加する
            steps.push(newStep(arr, [n - 1])); // 末尾に移動した要素をハイライト
        }
        steps.push(newStep(arr)); // 最終状態
    }

    // =================================
    // イベントハンドラ
    // =================================

    function stopAllAnimations() {
        if (animationTimer) {
            // animationTimerがGSAPインスタンス（オブジェクト）かsetTimeoutのID（数値）かを判別
            if (typeof animationTimer === 'object' && typeof animationTimer.kill === 'function') {
                // GSAPアニメーションを停止
                animationTimer.kill();
            } else if (typeof animationTimer === 'number') {
                // setTimeoutをクリア
                clearTimeout(animationTimer);
            }
            animationTimer = null;
        }
        if (bogoRotationTween) {
            bogoRotationTween.kill();
            gsap.to(visualizationArea, { rotation: 0, duration: 0.5 });
            bogoRotationTween = null;
        }
        visualizationArea.classList.remove('waving');
        // アニメーションで動いていた要素を元の位置に戻す
        gsap.to(visualizationArea, { x: 0, duration: 0.2 });
    }

    sortStartBtn.addEventListener('click', function() {
        stopAllAnimations(); // まず全てのアニメーションを停止・リセット

        const sortType = sortTypeSelect.value;
        const numbersStr = numbersInput.value;
        if (!/^\d{8}$/.test(numbersStr)) {
            alert('8桁の数字を入力してください。');
            return;
        }
        const array = numbersStr.split('').map(Number);

        sortSteps = [];
        animationFrame = 0;
        const arrToSort = [...array];

        switch (sortType) {
            case 'bubble':    bubbleSort(arrToSort, sortSteps);    break;
            case 'selection': selectionSort(arrToSort, sortSteps); break;
            case 'insertion': insertionSort(arrToSort, sortSteps); break;
            case 'merge':     mergeSort(arrToSort, sortSteps);     break;
            case 'quick':     quickSort(arrToSort, sortSteps);     break;
            case 'bogo':
                bogoSort(arrToSort, sortSteps);
                bogoRotationTween = gsap.to(visualizationArea, { rotation: 360, duration: 2, ease: "none", repeat: -1 });
                break;
            case 'stalin':    stalinSort(arrToSort, sortSteps);    break;
            case 'wave':      
                waveSort(arrToSort, sortSteps);      
                visualizationArea.classList.add('waving');
                break;
        }
        animateSort(); // 通常のソートアニメーションを開始
    });

    generateRandomBtn.addEventListener('click', () => {
        stopAllAnimations();
        let randomStr = '';
        const digits = [1, 2, 3, 4, 5, 6, 7, 8];
        while (digits.length > 0) {
            const randIndex = Math.floor(Math.random() * digits.length);
            randomStr += digits.splice(randIndex, 1)[0];
        }
        numbersInput.value = randomStr;
        drawBars(randomStr.split('').map(Number));
    });

    stopAnimationBtn.addEventListener('click', stopAllAnimations);
    sortTypeSelect.addEventListener('change', updateSortDescription);

    // 初期化
    updateSortDescription();
    generateRandomBtn.click();
});
