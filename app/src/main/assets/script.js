// получаем свои данные с чека и вставляем в эту переменную
let qrCode = Android.getQrCode();
// вставляет данные в сторку
document.querySelector('#b-checkform_qrraw').value = qrCode;
// удаляет активную вкладку
document.querySelector('.b-checkform_nav li.active').className = '';
// активирует вкладку со строкой
document.querySelectorAll('.b-checkform_nav li')[3].className = 'active';
// событие нажатия на кнопку "Проверить"
document.querySelector('.b-checkform_btn-send').dispatchEvent(new Event('click'));
// события нажатия на кнопку сохранения чека в формате json
let observable = document.querySelector('.b-check_place');
let checker = setInterval(() => {
    if (observable.classList.contains("hidden")) {
        console.log("not download yet");
    } else {
        clearInterval(checker);
        console.log("already downloaded");
        document.querySelector('.b-check_btn-json').dispatchEvent(new Event('click'));
    }
}, 1000);

Android.putJsonResult("ok");