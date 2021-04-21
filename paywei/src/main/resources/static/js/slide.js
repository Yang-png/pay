;(function ($) {
    $.fn.keyboard = function (options) {
        var bodyW = document.documentElement.clientWidth || document.body.clientWidth;
        var itemWidth = (bodyW - 40) / 10;
        var keyBoard = '<div id="keycontent">\n' +
            '<div id="keyboard">\n' +
            '    <div class="keyTitle">\n' +
            '        <div class="keyText">正在使用键盘</div>\n' +
            '        <div class="keyHide">收起</div>\n' +
            '    </div>\n' +
            '    <div class="keyContent"></div>\n' +
            ' </div>\n' +
            '</div>';
        if (!($("#keycontent").length > 0)) {
            $('body').append(keyBoard);
        }
        var defaults = {defaults: 'English', inputClass: 'text', caseSwitch: 'toUpperCase',};
        var endOptions = $.extend(defaults, options);
        this.each(function () {
            var _this = $('#keycontent');
            _this.on('click', 'li', function () {
                inputVal($(this));
                keyState($(this));
            });
            caseSwitch(defaults.defaults);
            _this.on('click', '.englishKeyboard', function () {
                caseSwitch('English');
                keyState($(this));
            });
            _this.on('click', '.symbolSwitch', function () {
                caseSwitch('symbol');
                keyState($(this));
            });
            _this.on('click', '.keyHide,.keyMask', function () {
                _this.remove();
            });

            function inputVal(_this) {
                let oDiv = $('.' + defaults.inputClass + '').val();
                let val = oDiv += _this.html();
                $('.' + defaults.inputClass + '').val(val);
            }

            function caseSwitch(data) {
                if (data == 'English') {
                    english(defaults.caseSwitch);
                } else if (data == 'number') {
                    number();
                } else if (data == 'symbol') {
                    symbol();
                }
            }

            function english(data) {
                _this.find('.keyContent').html('');
                let englishArray = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'];
                let english = '';
                english += '<ul class="english">';
                for (let i = 0; i < englishArray.length; i++) {
                    if (i == 20) {
                        english += "<li class='item' style='width: " + itemWidth + "px;margin-left:" + (itemWidth / 2 + 2 * 2) + "px'>" + englishArray[i] + "</li>"
                    } else if (i == 29) {
                        english += "<li class='item' style='width: " + itemWidth + "px;margin-left:" + (itemWidth + itemWidth / 2 + 2 * 4) + "px'>" + englishArray[i] + "</li>"
                    } else {
                        english += "<li class='item' style='width: " + itemWidth + "px'>" + englishArray[i] + "</li>"
                    }
                }
                english += '</ul>';
                english += '<div class="symbolSwitch" style="width: ' + (itemWidth + itemWidth / 2 + 2) + 'px">省份</div>';
                english += '<div class="del" style="width: ' + (itemWidth + itemWidth / 2 + 2) + 'px">删除</div>';
                _this.find('.keyContent').append(english);
            }

            function symbol(data) {
                _this.find('.keyContent').html('');
                let symbolArray = ['京', '津', '渝', '沪', '冀', '晋', '辽', '吉', '黑', '苏', '浙', '皖', '闽', '赣', '鲁', '豫', '鄂', '湘', '粤', '琼', '川', '贵', '云', '陕', '甘', '青', '蒙', '桂', '宁', '新', '藏', '使', '领', '警', '学', '港', '澳'];
                let english = '';
                english += '<ul class="english">';
                for (let i = 0; i < symbolArray.length; i++) {
                    if (i == 30) {
                        english += "<li class='item' style='width: " + itemWidth + "px;margin-left:" + (itemWidth + itemWidth / 2 + 2 * 4) + "px'>" + symbolArray[i] + "</li>"
                    } else {
                        english += "<li class='item' style='width: " + itemWidth + "px'>" + symbolArray[i] + "</li>"
                    }
                }
                english += '</ul>';
                english += '<div class="englishKeyboard" style="width: ' + (itemWidth + itemWidth / 2 + 2) + 'px">ABC</div>';
                english += '<div class="del" style="width: ' + (itemWidth + itemWidth / 2 + 2) + 'px">删除</div>';
                _this.find('.keyContent').append(english);
            }

            function keyState(data) {
                data.css('opacity', '0.3')
                setTimeout(function () {
                    data.css('opacity', '1')
                }, 100);
            }
        });
    };
})(jQuery);