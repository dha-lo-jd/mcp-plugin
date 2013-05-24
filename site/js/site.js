$(document).ready(function() {
    //ハッシュリンクのアンカータグをクリックするとマッチするidを持つ要素にスクロールする
    $('a[href^="#"]').click(function(event) {
        var id = $(this).attr("href");
        var offset = 20;
        var target = $(id).offset().top - offset;
        $('html, body').animate({scrollTop:target}, 100);
        event.preventDefault();
        return false;
    });
});