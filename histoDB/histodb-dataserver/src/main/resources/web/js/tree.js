/* ===========================================================
 * bootstrap-tooltip.js v2.0.4
 * http://twitter.github.com/bootstrap/javascript.html#tooltips
 * Inspired by the original jQuery.tipsy by Jason Frame
 * ===========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */


!function ($) {

    "use strict"; // jshint ;_;


    /* TOOLTIP PUBLIC CLASS DEFINITION
     * =============================== */

    var Tooltip = function (element, options) {
        this.init('tooltip', element, options)
    }

    Tooltip.prototype = {

        constructor: Tooltip

        , init: function (type, element, options) {
            var eventIn
                , eventOut

            this.type = type
            this.$element = $(element)
            this.options = this.getOptions(options)
            this.enabled = true

            if (this.options.trigger != 'manual') {
                eventIn  = this.options.trigger == 'hover' ? 'mouseenter' : 'focus'
                eventOut = this.options.trigger == 'hover' ? 'mouseleave' : 'blur'
                this.$element.on(eventIn, this.options.selector, $.proxy(this.enter, this))
                this.$element.on(eventOut, this.options.selector, $.proxy(this.leave, this))
            }

            this.options.selector ?
                (this._options = $.extend({}, this.options, { trigger: 'manual', selector: '' })) :
                this.fixTitle()
        }

        , getOptions: function (options) {
            options = $.extend({}, $.fn[this.type].defaults, options, this.$element.data())

            if (options.delay && typeof options.delay == 'number') {
                options.delay = {
                    show: options.delay
                    , hide: options.delay
                }
            }

            return options
        }

        , enter: function (e) {
            var self = $(e.currentTarget)[this.type](this._options).data(this.type)

            if (!self.options.delay || !self.options.delay.show) return self.show()

            clearTimeout(this.timeout)
            self.hoverState = 'in'
            this.timeout = setTimeout(function() {
                if (self.hoverState == 'in') self.show()
            }, self.options.delay.show)
        }

        , leave: function (e) {
            var self = $(e.currentTarget)[this.type](this._options).data(this.type)

            if (this.timeout) clearTimeout(this.timeout)
            if (!self.options.delay || !self.options.delay.hide) return self.hide()

            self.hoverState = 'out'
            this.timeout = setTimeout(function() {
                if (self.hoverState == 'out') self.hide()
            }, self.options.delay.hide)
        }

        , show: function () {
            var $tip
                , inside
                , pos
                , actualWidth
                , actualHeight
                , placement
                , tp

            if (this.hasContent() && this.enabled) {
                $tip = this.tip()
                this.setContent()

                if (this.options.animation) {
                    $tip.addClass('fade')
                }

                placement = typeof this.options.placement == 'function' ?
                    this.options.placement.call(this, $tip[0], this.$element[0]) :
                    this.options.placement

                inside = /in/.test(placement)

                $tip
                    .remove()
                    .css({ top: 0, left: 0, display: 'block' })
                    .appendTo(inside ? this.$element : document.body)

                pos = this.getPosition(inside)

                actualWidth = $tip[0].offsetWidth
                actualHeight = $tip[0].offsetHeight

                switch (inside ? placement.split(' ')[1] : placement) {
                    case 'bottom':
                        tp = {top: pos.top + pos.height, left: pos.left + pos.width / 2 - actualWidth / 2}
                        break
                    case 'top':
                        tp = {top: pos.top - actualHeight, left: pos.left + pos.width / 2 - actualWidth / 2}
                        break
                    case 'left':
                        tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left - actualWidth}
                        break
                    case 'right':
                        tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left + pos.width}
                        break
                }

                $tip
                    .css(tp)
                    .addClass(placement)
                    .addClass('in')
            }
        }

        , isHTML: function(text) {
            // html string detection logic adapted from jQuery
            return typeof text != 'string'
                || ( text.charAt(0) === "<"
                    && text.charAt( text.length - 1 ) === ">"
                    && text.length >= 3
                    ) || /^(?:[^<]*<[\w\W]+>[^>]*$)/.exec(text)
        }

        , setContent: function () {
            var $tip = this.tip()
                , title = this.getTitle()

            $tip.find('.tooltip-inner')[this.isHTML(title) ? 'html' : 'text'](title)
            $tip.removeClass('fade in top bottom left right')
        }

        , hide: function () {
            var that = this
                , $tip = this.tip()

            $tip.removeClass('in')

            function removeWithAnimation() {
                var timeout = setTimeout(function () {
                    $tip.off($.support.transition.end).remove()
                }, 500)

                $tip.one($.support.transition.end, function () {
                    clearTimeout(timeout)
                    $tip.remove()
                })
            }

            $.support.transition && this.$tip.hasClass('fade') ?
                removeWithAnimation() :
                $tip.remove()
        }

        , fixTitle: function () {
            var $e = this.$element
            if ($e.attr('title') || typeof($e.attr('data-original-title')) != 'string') {
                $e.attr('data-original-title', $e.attr('title') || '').removeAttr('title')
            }
        }

        , hasContent: function () {
            return this.getTitle()
        }
        /*
         , getPosition: function (inside) {
         return $.extend({}, (inside ? {top: 0, left: 0} : this.$element.offset()), {
         width: this.$element[0].offsetWidth
         , height: this.$element[0].offsetHeight
         })
         }
         */
        , getPosition: function (inside) {
            var rect = getScreenBBox(this.$element[0])
            return {left: rect.x, top: rect.y, width:rect.width, height:rect.height}
        }
        , getTitle: function () {
            var title
                , $e = this.$element
                , o = this.options

            title = $e.attr('data-original-title')
                || (typeof o.title == 'function' ? o.title.call($e[0]) :  o.title)

            return title
        }

        , tip: function () {
            return this.$tip = this.$tip || $(this.options.template)
        }

        , validate: function () {
            if (!this.$element[0].parentNode) {
                this.hide()
                this.$element = null
                this.options = null
            }
        }

        , enable: function () {
            this.enabled = true
        }

        , disable: function () {
            this.enabled = false
        }

        , toggleEnabled: function () {
            this.enabled = !this.enabled
        }

        , toggle: function () {
            this[this.tip().hasClass('in') ? 'hide' : 'show']()
        }

    }


    /* TOOLTIP PLUGIN DEFINITION
     * ========================= */

    $.fn.svgTooltip = function ( option ) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('tooltip')
                , options = typeof option == 'object' && option
            if (!data) $this.data('tooltip', (data = new Tooltip(this, options)))
            if (typeof option == 'string') data[option]()
        })
    }

    $.fn.svgTooltip.Constructor = Tooltip

    $.fn.svgTooltip.defaults = {
        animation: true
        , placement: 'top'
        , selector: false
        , template: '<div class="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
        , trigger: 'hover'
        , title: ''
        , delay: 0
    }

}(window.jQuery);
/* ===========================================================
 * bootstrap-popover.js v2.0.4
 * http://twitter.github.com/bootstrap/javascript.html#popovers
 * ===========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================================================== */


!function ($) {

    "use strict"; // jshint ;_;


    /* POPOVER PUBLIC CLASS DEFINITION
     * =============================== */

    var Popover = function ( element, options ) {
        this.init('popover', element, options)
    }

    window.PopOver = Popover

    /* NOTE: POPOVER EXTENDS BOOTSTRAP-TOOLTIP.js
     ========================================== */

    Popover.prototype = $.extend({}, $.fn.svgTooltip.Constructor.prototype, {

        constructor: Popover

        , setContent: function () {
            var $tip = this.tip()
                , title = this.getTitle()
                , content = this.getContent()

            $tip.find('.popover-title')[this.isHTML(title) ? 'html' : 'text'](title)
            $tip.find('.popover-content > *')[this.isHTML(content) ? 'html' : 'text'](content)

            $tip.removeClass('fade top bottom left right in')
        }

        , hasContent: function () {
            return this.getTitle() || this.getContent()
        }

        , getContent: function () {
            var content
                , $e = this.$element
                , o = this.options

            content = $e.attr('data-content')
                || (typeof o.content == 'function' ? o.content.call($e[0]) :  o.content)

            return content
        }

        , tip: function () {
            if (!this.$tip) {
                this.$tip = $(this.options.template)
            }
            return this.$tip
        }

    })


    /* POPOVER PLUGIN DEFINITION
     * ======================= */

    $.fn.svgPopover = function (option) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('popover')
                , options = typeof option == 'object' && option
            if (!data) $this.data('popover', (data = new Popover(this, options)))
            if (typeof option == 'string') data[option]()
        })
    }

    $.fn.svgPopover.Constructor = Popover

    $.fn.svgPopover.defaults = $.extend({} , $.fn.tooltip.defaults, {
        placement: 'right'
        , content: ''
        , template: '<div class="popover"><div class="arrow"></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>'
    })

}(window.jQuery);


/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */



/**
 * Utils
 */
(function ($) {


    taskDescriptors = {};
    templatedViews = [];

    registerTaskDescriptor = function (taskClass, taskView, resultView, taskType) {
        taskType = taskType || (taskClass.prototype && taskClass.prototype.defaults && taskClass.prototype.defaults.type);
        taskDescriptors[taskType] = {
            sectionClass: taskClass.prototype.defaults && taskClass.prototype.defaults.sectionClass,
            clazz: taskClass,
            view: taskView,
            resultView: resultView
        }

        if (taskView) registerTemplatedView(taskView)
        registerTemplatedView(resultView)
    }

    registerTemplatedView = function (view) {
        if (!view.prototype.initTemplate && !view.prototype.templateUrl)
        //throw "No template for" + view.name
            return;
        else templatedViews.push(view);
    }

    executeRPC = function (options, rpcMethod, args) {
        options || (options = {});
        var request = {
            "jsonrpc": "2.0",
            "id": 123,
            "method": rpcMethod,
            "params": (args || [])
        };

        // Default JSON-request options.
        var params = _.extend({
            url: '/rest/rpc/methods/' + encodeURIComponent(rpcMethod),
            type: "POST",
            dataType: "json",
            contentType: 'application/json',
            success: options.success || function (data) {
                if (data.error) {
                    options.rpcError && options.rpcError.call(this, data.error);
                } else {
                    options.rpcSuccess && options.rpcSuccess.call(this, data.result);
                }
            },
            data: JSON.stringify(request),
        }, options);

        return $.ajax(params);
    };

    formatNumber = function (x, precision) {
        var s = x.toPrecision(precision);
        if (s.indexOf(".") >= 0) {
            s = s.replace(/\.?0*$/, '');
        }
        return s;
    }

    //
    // COLORS
    //

    rgb2hsv = function (r, g, b) {
        var computedH = 0;
        var computedS = 0;
        var computedV = 0;

        r = r / 255;
        g = g / 255;
        b = b / 255;
        var minRGB = Math.min(r, Math.min(g, b));
        var maxRGB = Math.max(r, Math.max(g, b));

        // Black-gray-white
        if (minRGB === maxRGB) {
            computedV = minRGB;
            return {h: 0, s: 0, v: computed};
        }

        // Colors other than black-gray-white:
        var d = (r === minRGB) ? g - b : ((b === minRGB) ? r - g : b - r);
        var h = (r === minRGB) ? 3 : ((b === minRGB) ? 1 : 5);
        computedH = 60 * (h - d / (maxRGB - minRGB));
        computedS = (maxRGB - minRGB) / maxRGB;
        computedV = maxRGB;
        return {h: computedH, s: computedS, v: computedV};
    }

    hsv2rgb = function (h, s, v) {
        // Adapted from http://www.easyrgb.com/math.html
        // hsv values = 0 - 1, rgb values = 0 - 255

        var h = h / 360;

        var r, g, b;
        var RGB = {};
        if (s == 0) {
            RGB.r = RGB.g = RGB.b = Math.round(v * 255);
        } else {
            // h must be < 1
            var var_h = h * 6;
            if (var_h === 6) var_h = 0;
            //Or ... var_i = floor( var_h )
            var var_i = Math.floor(var_h);
            var var_1 = v * (1 - s);
            var var_2 = v * (1 - s * (var_h - var_i));
            var var_3 = v * (1 - s * (1 - (var_h - var_i)));
            if (var_i === 0) {
                var_r = v;
                var_g = var_3;
                var_b = var_1;
            } else if (var_i === 1) {
                var_r = var_2;
                var_g = v;
                var_b = var_1;
            } else if (var_i === 2) {
                var_r = var_1;
                var_g = v;
                var_b = var_3
            } else if (var_i === 3) {
                var_r = var_1;
                var_g = var_2;
                var_b = v;
            } else if (var_i === 4) {
                var_r = var_3;
                var_g = var_1;
                var_b = v;
            } else {
                var_r = v;
                var_g = var_1;
                var_b = var_2
            }
            //rgb results = 0 ÷ 255
            RGB.r = Math.round(var_r * 255);
            RGB.g = Math.round(var_g * 255);
            RGB.b = Math.round(var_b * 255);
        }
        return RGB;
    };

    toHex = function (n) {
        if (isNaN(n)) return "00";
        n = Math.max(0, Math.min(n, 255));
        return "0123456789ABCDEF".charAt((n - n % 16) / 16) + "0123456789ABCDEF".charAt(n % 16);
    }

    rgb2hex = function (R, G, B) {
        return toHex(R) + toHex(G) + toHex(B)
    }

    hex2rgb = function (h) {
        // strip sharp sign
        (h.charAt(0) != "#") || (h = h.substring(1, 7))
        var r = parseInt(h.substring(0, 2), 16);
        var g = parseInt(h.substring(2, 4), 16);
        var b = parseInt(h.substring(4, 6), 16);
        return {r: r, g: g, b: b}
    }

    interpolateHSVColor = function (rgb1, rgb2, n) {
        var hsv1 = rgb2hsv(rgb1.r, rgb1.g, rgb1.b);
        var hsv2 = rgb2hsv(rgb2.r, rgb2.g, rgb2.b);
        var colors = [];
        if (n == 1) {
            colors[0] = "#" + rgb2hex(rgb1.r, rgb1.g, rgb1.b)
        } else {
            for (var i = 0; i < n; i++) {
                var delta = i / (n - 1);
                var h = hsv2.h * delta + hsv1.h * (1 - delta)
                var s = hsv2.s * delta + hsv1.s * (1 - delta)
                var v = hsv2.v * delta + hsv1.v * (1 - delta)
                var rgb = hsv2rgb(h, s, v);
                colors.push("#" + rgb2hex(rgb.r, rgb.g, rgb.b));
            }
        }
        return colors;
    }

    function pad(n) {
        return n < 10 ? '0' + n : n
    }

    //
    // TIME
    //
    var SECOND = 1000;
    var MINUTE = 60 * SECOND;
    var HOUR = 60 * MINUTE;
    var DAY = 24 * HOUR;
    var MONTH = 30 * DAY;
    var YEAR = 365 * DAY;

    fromExcelTime = function (time) {
        return (time - 25569) * 86400000
    }

    fromUnixTime = function (time) {
        return time * 1000
    }

    toExcelTime = function (time) {
        return time / 86400000 + 25569
    }

    toUnixTime = function (time) {
        return time / 1000
    }

    formatTime = function (time, range) {
        var date = new Date();
        date.setTime(time);
        if (range > 5 * YEAR) {
            return date.getFullYear().toString();
        } else if (range > 3 * MONTH) {
            return pad(date.getMonth() + 1) + '/' + date.getFullYear();
        } else if (range > 7 * DAY) {
            return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + '/' + date.getFullYear();
        } else if (range > 3 * DAY) {
            return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + " " + pad(date.getHours());
        } else if (range > 1 * DAY) {
            return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + " " + pad(date.getHours()) + ":" + pad(date.getMinutes());
        } else if (range > 10 * SECOND) {
            return pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds());
        } else if (range) {
            return pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds()) + "." + date.getMilliseconds();
        } else {
            return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + '/' + date.getFullYear() + " " + pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" + pad(date.getSeconds());
        }
    }

    createTimeScale = function (t0, t1, minRange) {
        var scale = {values: [], labels: []};
        var d = new Date();
        d.setTime(t0);
        if (minRange < SECOND) {
            d.setMilliseconds(0);
            for (var t = d.getTime(); t <= t1; t += SECOND) {
                d.setTime(t);
                scale.values.push(t);
                scale.labels.push(pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()));
            }
        } else if (minRange < 10 * SECOND) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            for (var t = d.getTime(); t <= t1; t += 10 * SECOND) {
                d.setTime(t);
                scale.values.push(t);
                scale.labels.push(pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()));
            }
        } else if (minRange < MINUTE) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            for (var t = d.getTime(); t <= t1; t += MINUTE) {
                d.setTime(t);
                scale.values.push(t);
                scale.labels.push(pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()));
            }
        } else if (minRange < 10 * MINUTE) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            for (var t = d.getTime(); t <= t1; t += 10 * MINUTE) {
                d.setTime(t);
                scale.values.push(t);
                scale.labels.push(pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()));
            }
        } else if (minRange < HOUR) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            for (var t = d.getTime(); t <= t1; t += HOUR) {
                d.setTime(t);
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
            }
        } else if (minRange < 4 * HOUR) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            for (var t = d.getTime(); t <= t1; d.setDate(d.getDate() + 1)) {
                d.setHours(0);
                t = d.getTime();
                scale.values.push(t); // 0h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
                t += 4 * HOUR
                d.setTime(t);
                scale.values.push(t); // 4h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
                t += 4 * HOUR
                d.setTime(t);
                scale.values.push(t); // 8h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
                t += 4 * HOUR
                d.setTime(t);
                scale.values.push(t); // 12h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
                t += 4 * HOUR
                d.setTime(t);
                scale.values.push(t); // 16h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
                t += 4 * HOUR
                d.setTime(t);
                scale.values.push(t); // 20h
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
            }
        } else if (minRange < DAY) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            for (var t = d.getTime(); t <= t1; d.setDate(d.getDate() + 1)) {
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()));
            }
        } else if (minRange < 10 * DAY) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            for (var t = d.getTime(); t <= t1; d.setMonth(d.getMonth() + 1)) {
                d.setDate(1);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1));
                d.setDate(11);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1));
                d.setDate(21);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1));
            }
        } else if (minRange < MONTH) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            d.setDate(1);
            for (var t = d.getTime(); t <= t1; d.setMonth(d.getMonth() + 1)) {
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
            }
        } else if (minRange < 3 * MONTH) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            d.setDate(1);
            for (var t = d.getTime(); t <= t1; d.setFullYear(d.getFullYear() + 1)) {
                d.setMonth(0);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
                d.setMonth(3);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
                d.setMonth(6);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
                d.setMonth(9);
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
            }
        } else if (minRange < YEAR) {
            d.setMilliseconds(0);
            d.setSeconds(0);
            d.setMinutes(0);
            d.setHours(0);
            d.setDate(1);
            d.setMonth(0);
            for (var t = d.getTime(); t <= t1; d.setFullYear(d.getFullYear() + 1)) {
                t = d.getTime();
                scale.values.push(t);
                scale.labels.push(pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear());
            }
        } else {
            var magnitude = Math.pow(10, Math.ceil(Math.log(minRange) / Math.LN10));
            for (var t = Math.floor(t0 / magnitude); t <= t1; t += magnitude) {
                scale.values.push(t);
                scale.labels.push("" + t);
            }
        }
        return scale;
    }

    /**
     * Divide an entire phrase in an array of phrases, all with the max pixel length given.
     * The words are initially separated by the space char.
     * @param phrase
     * @param length
     * @return
     */
    wrapText = function (ctx, text, lineWidth) {
        var wa = text.split(" ")
        var position = 0
        var phrases = [""];
        var index = 0;
        for (var i = 0; i < wa.length; i++) {
            var measure = ctx.measureText(wa[i]).width;
            if (position != 0 && (position + measure) >= lineWidth) {
                phrases [++index] = ""
                position = 0
            }
            phrases[index] += wa[i] + " "
            position += measure
        }
        return phrases;
    }

    gaussian = function (x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI)
    }

    erf = function (x) {
        var b0 = 0.2316419
        var b1 = 0.319381530
        var b2 = -0.356563782
        var b3 = 1.781477937
        var b4 = -1.821255978
        var b5 = 1.330274429
        var t = 1 / (1 + b0 * x)
        return 1 - gaussian(x) * (t * (b1 + t * (b2 + t * (b3 + t * (b4 + t * b5)))))
    }

    normalDistribution = function (x, mu, sigma) {
        return gaussian((x - mu) / sigma) / sigma
    }

    cumulativeNormalDistribution = function (x, mu, sigma) {
        return 0.5 * (1 + erf((x - mu) / (sigma * Math.sqrt(2))))
    }

    /**
     Inheritance
     */
    Class = function () {
        if (this.initialize) {
            this.initialize.apply(this, arguments)
        }
    }

    Class.extend = Backbone.Model.extend

    /**
     Mouse tool
     */
    MouseTool = Class.extend({
        events: [ "dblclick", "click", "mouseup", "mousedown", "mousemove", "mousewheel", "mouseout", "mouseover"],
        bind: function ($selector) {
            this.$selector = $selector
            this.handlers = {}
            var self = this
            _.each(this.events, function (ev) {
                if (self[ev]) {
                    var cb = _.bind(self[ev], self)
                    self.handlers[ev] = cb
                    self.$selector.on(ev, cb)
                }
            })
        },
        unbind: function () {
            var self = this
            _.each(this.handlers, function (cb, ev) {
                self.$selector.off(ev, cb)
            })
        },
    }, {
        fillEventOffsets: function (ev) {
            if (ev.offsetX || ev.offsetX === 0) {
                ev._x = ev.offsetX;
                ev._y = ev.offsetY;
            } else {
                var offset = $(ev.target).offset();
                ev._x = ev.pageX - offset.left;
                ev._y = ev.pageY - offset.top;
            }
            return ev;
        }
    })

    DEFAULT_COLORS = [
        "#3366cc",
        "#dc3912",
        "#ff9900",
        "#109618",
        "#990099",
        "#0099c6",
        "#dd4477",
        "#66aa00",
        "#b82e2e",
        "#316395",
        "#994499",
        "#22aa99",
        "#aaaa11",
        "#6633cc",
        "#e67300",
        "#0c5922",
        "#bea413",
        "#668d1c",
        "#2a778d",
        "#a9c413",
        "#9c5935",
        "#f4359e",
        "#b91383",
        "#16d620",
        "#b77322",
        "#3b3eac",
        "#5574a6",
        "#329262",
        "#651067",
        "#8b0707"]

//	GRADIENT_COLORS_5 = interpolateHSVColor(hex2rgb("#4242ee"), hex2rgb("#ee4242"), 5)
    GRADIENT_COLORS_5 = [DEFAULT_COLORS[0], DEFAULT_COLORS[3], "#EEDB42", DEFAULT_COLORS[2], DEFAULT_COLORS[1]]

    COLOR_YIN = DEFAULT_COLORS[3]
    COLOR_YANG = DEFAULT_COLORS[2]

    ColorMap = Class.extend({
        initialize: function () {
            this.colorIndex = 0;
            this.map = {
                "yes": COLOR_YIN,
                "no": COLOR_YANG,
                "ok": COLOR_YIN,
                "nok": COLOR_YANG,
                "fail": COLOR_YANG,
                "plus": COLOR_YIN,
                "minus": COLOR_YANG,
                "good": COLOR_YIN,
                "bad": COLOR_YANG,
                "true": COLOR_YIN,
                "false": COLOR_YANG,

                "very low": GRADIENT_COLORS_5[0],
                "low": GRADIENT_COLORS_5[1],
                "average": GRADIENT_COLORS_5[2],
                "medium": GRADIENT_COLORS_5[2],
                "high": GRADIENT_COLORS_5[3],
                "very high": GRADIENT_COLORS_5[4],

                "very cold": GRADIENT_COLORS_5[0],
                "cold": GRADIENT_COLORS_5[1],
                "mild": GRADIENT_COLORS_5[2],
                "hot": GRADIENT_COLORS_5[3],
                "very hot": GRADIENT_COLORS_5[4],
                "warm": GRADIENT_COLORS_5[3],
                "very warm": GRADIENT_COLORS_5[4],

                "january": DEFAULT_COLORS[0],
                "february": DEFAULT_COLORS[1],
                "march": DEFAULT_COLORS[2],
                "april": DEFAULT_COLORS[3],
                "may": DEFAULT_COLORS[4],
                "june": DEFAULT_COLORS[5],
                "july": DEFAULT_COLORS[6],
                "august": DEFAULT_COLORS[7],
                "september": DEFAULT_COLORS[8],
                "october": DEFAULT_COLORS[9],
                "november": DEFAULT_COLORS[10],
                "december": DEFAULT_COLORS[11],

                "winter": DEFAULT_COLORS[0],
                "spring": DEFAULT_COLORS[3],
                "summer": DEFAULT_COLORS[1],
                "fall": DEFAULT_COLORS[2],
                "mid-season": DEFAULT_COLORS[3],
                "intra": DEFAULT_COLORS[3],

                "q1": DEFAULT_COLORS[0],
                "q2": DEFAULT_COLORS[3],
                "q3": DEFAULT_COLORS[1],
                "q4": DEFAULT_COLORS[2],

                "day": DEFAULT_COLORS[3],
                "night": DEFAULT_COLORS[0],

                "monday": DEFAULT_COLORS[0],
                "tuesday": DEFAULT_COLORS[1],
                "wednesday": DEFAULT_COLORS[2],
                "thursday": DEFAULT_COLORS[3],
                "friday": DEFAULT_COLORS[4],
                "saturday": DEFAULT_COLORS[5],
                "sunday": DEFAULT_COLORS[6],

                "2018": DEFAULT_COLORS[10],
                "2017": DEFAULT_COLORS[9],
                "2016": DEFAULT_COLORS[8],
                "2015": DEFAULT_COLORS[5],
                "2014": DEFAULT_COLORS[4],
                "2013": DEFAULT_COLORS[0],
                "2012": DEFAULT_COLORS[1],
                "2011": DEFAULT_COLORS[2],
                "2010": DEFAULT_COLORS[3],
                "2009": DEFAULT_COLORS[6],
                "2008": DEFAULT_COLORS[7],
            }
        },
        getColor: function (symbol) {
            if (symbol) {
                symbol = symbol.toLowerCase()
                if (this.map[symbol]) {
                    return this.map[symbol]
                } else {
                    var c = DEFAULT_COLORS[this.colorIndex % DEFAULT_COLORS.length]
                    this.colorIndex++;
                    this.map[symbol] = c
                    return c
                }
            } else {
                return "#ffffff"
            }
        }

    })

    escapeSymbol = function (symbol) {
        var symbolRegExp = /[ \t\(\)\:\#"'\,]/i
        if (symbolRegExp.test(symbol)) {
            symbol = "|" + symbol + "|"
        }
        return symbol
    }

    Array.prototype.remove = function (elem) {
        var idx = this.indexOf(elem)
        if (idx >= 0) return this.splice(idx, 1)
    }
    Array.prototype.add = function (elem) {
        var idx = this.indexOf(elem)
        if (idx < 0) this.push(elem)
    }
    Array.prototype.addAll = function (arr) {
        _.each(arr, this.add, this)
    }


    $.expr[':'].iContains = function (obj, params, meta, stack) {
        var opts = meta[3].match(/(.*)\s*,\s*(.*)/);
        return (opts[1] in obj) && (obj[opts[1]].toLowerCase().indexOf(opts[2].toLowerCase()) >= 0);
    };


    /* AFFIX CLASS DEFINITION
     * ====================== */

    var myAffix = function (element, options) {
        this.options = $.extend({}, $.fn.myaffix.defaults, options)
        this.$element = $(element)
        this.$window = this.$element.scrollParent()
            .on('scroll.affix.data-api', $.proxy(this.checkPosition, this))
        //.on('click.affix.data-api',  $.proxy(function () { setTimeout($.proxy(this.checkPosition, this), 1) }, this))
        this.checkPosition()
    }

    myAffix.prototype.checkPosition = function () {
        if (!this.$element.is(':visible')) return

        var scrollHeight = $(document).height()
            , scrollTop = this.$window.scrollTop()
            , position = this.$element.offset()
            , offset = this.options.offset
            , offsetBottom = offset.bottom
            , offsetTop = offset.top
            , reset = 'affix affix-top affix-bottom'
            , affix

        if (typeof offset != 'object') offsetBottom = offsetTop = offset
        if (typeof offsetTop == 'function') offsetTop = offset.top()
        if (typeof offsetBottom == 'function') offsetBottom = offset.bottom()

        affix = this.unpin != null && (scrollTop + this.unpin <= position.top) ?
            false : offsetBottom != null && (position.top + this.$element.height() >= scrollHeight - offsetBottom) ?
            'bottom' : offsetTop != null && scrollTop <= offsetTop ?
            'top' : false

        if (this.affixed === affix) return

        this.affixed = affix
        this.unpin = affix == 'bottom' ? position.top - scrollTop : null

        this.$element.removeClass(reset).addClass('affix' + (affix ? '-' + affix : ''))
    }


    /* myAFFIX PLUGIN DEFINITION
     * ======================= */


    $.fn.myaffix = function (option) {
        return this.each(function () {
            var $this = $(this)
                , data = $this.data('affix')
                , options = typeof option == 'object' && option
            if (!data) $this.data('affix', (data = new myAffix(this, options)))
            if (typeof option == 'string') data[option]()
        })
    }

    $.fn.myaffix.Constructor = myAffix

    $.fn.myaffix.defaults = {
        offset: 0
    }


})(jQuery)


/**
 * SVG Utils
 *
 */

svg = function (elem, attributes) {
    var elem = document.createElementNS('http://www.w3.org/2000/svg', elem)
    for (var attr in attributes) {
        elem.setAttributeNS(null, attr, attributes[attr]);
    }
    function append(arg) {
        if (arg instanceof SVGElement) {
            elem.appendChild(arg)
        } else if (arg instanceof Element) {
            elem.appendChild(arg)
        } else if (Array.isArray(arg)) {
            for (var j = 0; j < arg.length; j++) {
                append(arg[j])
            }
        } else if (arg && arg.toString) {
            elem.appendChild(document.createTextNode(arg.toString()))
        }
    }

    for (var i = 2; i < arguments.length; i++) {
        append(arguments[i])
    }
    return elem
}

SVGCanvas = Class.extend({
    initialize: function (svg) {
        this.element = svg
        this.width = $(svg).attr('width')
        this.height = $(svg).attr('height')
        this.context = new SVGContext(this)
    },
    getContext: function () {
        return this.context
    }
})

var textAlign = {"left": "start", "start": "start", "right": "end", "end": "end", "center": "middle"}
var textBaseline = {"top": "text-before-edge", "hanging": "hanging", "middle": "central", "alphabetic": "alphabetic", "ideographic": "alphabetic", "bottom": "text-after-edge"}

SVGContext = Class.extend({
    initialize: function (svgCanvas) {
        this.contexts = [
            {}
        ];
        this.current = svgCanvas.element
    },
    append: function (elem) {
        this.current.appendChild(elem)
    },
    save: function () {
        this.contexts.push({
            fillStyle: this.fillStyle,
            strokeStyle: this.strokeStyle,
            lineWidth: this.lineWidth,
            font: this.font,
            textAlign: this.textAlign,
            textBaseline: this.textBaseline,
            clipPath: this.clipPath})
    },
    restore: function () {
        var ctx = this.contexts.pop()
        this.fillStyle = ctx.fillStyle
        this.strokeStyle = ctx.strokeStyle,
            this.lineWidth = ctx.lineWidth,
            this.font = ctx.font,
            this.textAlign = ctx.textAlign,
            this.textBaseline = ctx.textBaseline
        this.clipPath = ctx.clipPath
    },
    clip: function () {
        var id = "clip" + new Date().getTime()
        var clipPath = this.elem("clipPath", {id: id},
            this.elem("path", {d: this.currentPath.join("")}))
        this.append(clipPath)
        this.clipPath = 'url(#' + id + ')'
    },
    rect: function (x, y, w, h) {
        this.moveTo(x, y)
        this.lineTo(x + w, y)
        this.lineTo(x + w, y + h)
        this.lineTo(x, y + h)
        this.closePath()
    },
    elem: function (elem, attributes) {
        return svg.apply(null, arguments)
    },
    fillRect: function (x, y, width, height) {
        var rect = this.elem('rect', {x: x, y: y, height: height, width: width, fill: this.fillStyle, "clip-path": this.clipPath});
        this.append(rect)
    },
    strokeRect: function (x, y, width, height) {
        var rect = this.elem('rect', {
            x: x,
            y: y,
            height: height,
            width: width,
            stroke: this.strokeStyle,
            "stroke-width": this.lineWidth,
            fill: "none",
            "clip-path": this.clipPath});
        this.append(rect)
    },
    beginPath: function () {
        this.currentPath = []
    },
    moveTo: function (x, y) {
        this.currentPath.push(" M" + x + " " + y)
    },
    lineTo: function (x, y) {
        this.currentPath.push(" L" + x + " " + y)
    },
    closePath: function () {
        this.currentPath.push(" Z")
    },
    stroke: function () {
        var path = this.elem("path", {
            d: this.currentPath.join(""),
            stroke: this.strokeStyle,
            "stroke-width": this.lineWidth,
            fill: "none",
            "clip-path": this.clipPath
        })
        this.append(path)
    },
    fill: function () {
        var path = this.elem("path", {d: this.currentPath.join(""), fill: this.fillStyle, "clip-path": this.clipPath})
        this.append(path)
    },
    getTextStyle: function () {
        var style = ""
        if (this.font) {
            style += "font:" + this.font
        }
        if (this.fillStyle) {
            style += ";fill:" + this.fillStyle
        }
        if (this.textAlign) {
            style += ";text-anchor: " + textAlign[this.textAlign]
        }
        if (this.textBaseline) {
            style += ";dominant-baseline: " + textBaseline[this.textBaseline]
        }
        return style
    },
    fillText: function (textString, x, y) {
        var text = this.elem('text', {x: x, y: y, style: this.getTextStyle(), "clip-path": this.clipPath}, textString)
        this.append(text)
    },
    measureText: function (textString) {
        var text = this.elem('text', {style: this.getTextStyle(), visibility: "hidden"}, textString)
        this.append(text)
        var width = text.getComputedTextLength()
        text.parentNode.removeChild(text)
        return {width: width}
    },
    getImageData: function () {
        return {}
    }
})

SVGMouseTool = MouseTool.extend({
    enablePan: 1, // 1 or 0: enable or disable panning (default enabled)
    enableZoom: 1, // 1 or 0: enable or disable zooming (default enabled)
    enableDrag: 0, // 1 or 0: enable or disable dragging (default disabled)
    zoomScale: 0.01, // Zoom sensitivity
    initialize: function ($root, $selector) {
        this.state = 'none'
        this.svgRoot = $selector[0]
        this.stateTarget = null
        this.stateOrigin = null
        this.stateTf = null
        this.root = $root[0];
    },
    /**
     * Sets the current transform matrix of an element.
     */
    setCTM: function (element, matrix) {
        var s = "matrix(" + matrix.a + "," + matrix.b + "," + matrix.c + "," + matrix.d + "," + matrix.e + "," + matrix.f + ")";
        element.setAttribute("transform", s);
    },
    /**
     * Instance an SVGPoint object with given event coordinates.
     */
    getEventPoint: function (evt) {
        MouseTool.fillEventOffsets(evt)
        var p = this.root.createSVGPoint();
        p.x = evt._x;
        p.y = evt._y;
        return p;
    },
    mousewheel: function (evt, delta) {

        delta = delta || Math.min(2,evt.originalEvent.wheelDelta)

        if (!this.enableZoom)
            return;

        if (evt.preventDefault)
            evt.preventDefault();
        evt.returnValue = false;

        var z = Math.pow(1 + this.zoomScale, delta);

        var g = this.svgRoot;

        var p = this.getEventPoint(evt.originalEvent);

        p = p.matrixTransform(g.getCTM().inverse());

        // Compute new scale matrix in current mouse position
        var k = this.root.createSVGMatrix().translate(p.x, p.y).scale(z).translate(-p.x, -p.y);

        this.setCTM(g, g.getCTM().multiply(k));

//			if(typeof(this.stateTf) == "undefined") // WTF ???????
        if (!this.stateTf)
            this.stateTf = g.getCTM().inverse();

        this.stateTf = this.stateTf.multiply(k.inverse());
    },
    zoomByFactor: function (factor) {
        var g = this.svgRoot;

        var bb = g.getBBox()

        var xc = bb.x + bb.width / 2.0
        var yc = bb.y + bb.height / 2.0

        // Compute new scale matrix in current mouse position
        var k = this.root.createSVGMatrix().translate(xc, yc).scale(factor).translate(-xc, -yc);

        this.setCTM(g, g.getCTM().multiply(k));

//			if(typeof(this.stateTf) == "undefined") // WTF ???????
        if (!this.stateTf)
            this.stateTf = g.getCTM().inverse();

        this.stateTf = this.stateTf.multiply(k.inverse());
    },
    mousemove: function (evt) {
        if (evt.preventDefault)
            evt.preventDefault();
        evt.returnValue = false;

        var g = this.svgRoot;

        if (this.state == 'pan' && this.enablePan) {
            // Pan mode
            var p = this.getEventPoint(evt).matrixTransform(this.stateTf);

            this.setCTM(g, this.stateTf.inverse().translate(p.x - this.stateOrigin.x, p.y - this.stateOrigin.y));
        } else if (this.state == 'drag' && this.enableDrag) {
            // Drag mode
            var p = this.getEventPoint(evt).matrixTransform(g.getCTM().inverse());

            this.setCTM(this.stateTarget, this.root.createSVGMatrix().translate(p.x - this.stateOrigin.x, p.y - this.stateOrigin.y).multiply(g.getCTM().inverse()).multiply(this.stateTarget.getCTM()));

            this.stateOrigin = p;
        }
    },
    mousedown: function (evt) {
        if (evt.preventDefault)
            evt.preventDefault();
        evt.returnValue = false;

        var g = this.svgRoot;

        if (
            evt.target.tagName == "svg"
            || !this.enableDrag // Pan anyway when drag is disabled and the user clicked on an element
            ) {
            // Pan mode
            this.state = 'pan';

            this.stateTf = g.getCTM().inverse();

            this.stateOrigin = this.getEventPoint(evt).matrixTransform(this.stateTf);
        } else {
            // Drag mode
            this.state = 'drag';

            this.stateTarget = evt.target;

            this.stateTf = g.getCTM().inverse();

            this.stateOrigin = this.getEventPoint(evt).matrixTransform(this.stateTf);
        }
    },
    mouseup: function (evt) {
        if (evt.preventDefault)
            evt.preventDefault();
        evt.returnValue = false;

        if (this.state == 'pan' || this.state == 'drag') {
            // Quit pan mode
            this.state = '';
        }
    },
    fitToBounds: function (bounds) {
        var svgWidth = this.root.width.baseVal.value
        var svgHeight = this.root.height.baseVal.value
        var scale = Math.min(1.0, svgWidth / bounds.width, svgHeight / bounds.height)
        var m = this.root.createSVGMatrix().translate(svgWidth / 2.0, svgHeight / 2.0)
            .scale(scale)
            .translate(-(bounds.offsetX + bounds.width / 2.0), -(bounds.offsetY + bounds.height / 2.0))

        this.setCTM(this.svgRoot, m)
    }
})

getScreenBBox = function (elem) {
    var bb = elem.getBBox()
    var ctm = elem.getScreenCTM()
    var p = elem.ownerSVGElement.createSVGPoint()
    p.x = bb.x
    p.y = bb.y
    var p1 = p.matrixTransform(ctm)
    p.x = bb.x + bb.width
    p.y = bb.y + bb.height
    var p2 = p.matrixTransform(ctm)
    var rect = elem.ownerSVGElement.createSVGRect()
    rect.x = p1.x
    rect.y = p1.y
    rect.width = p2.x - p1.x
    rect.height = p2.y - p1.y
    return rect
}

DecisionTreeView = function(container) {
    this.$ = container
    this.colorMap = new ColorMap()
}

_.extend(DecisionTreeView.prototype,
{

    renderResult: function (result) {
        this.result = result

        var $content = this.$.empty()
        $content.html($("#treePanel").html())

        /*
        this.$(".export-btn").click(_.bind(this.export, this))
        this.$(".export-svg-btn").click(_.bind(this.exportSVG, this))
        this.$(".export-pdf-btn").click(_.bind(this.exportPDF, this))
        this.$(".actions").show()
        */

        var width = $content.width()
        var height = $content.height()

        this.$svg = $(document.createElementNS('http://www.w3.org/2000/svg', 'svg'))
        this.$svg.attr("width", width).attr("height", height)

        $content.find(".tree-graph").append(this.$svg) /*.resizable({stop: _.bind(this.onResize, this)}); */

        this.isClassificationTree = this.result.tree.type === "classification"

        this.fillTreeNodeStats(this.result.tree.root, this.result.stats)

        if (this.result.testStats) {
            this.fillTreeNodeStats(this.result.tree.root, this.result.testStats)
        }

        this.root = this.result.tree.root
        this.drawTree()

        /* this.$svg.disableSelection(); */

        $(".tree-node-edit-dialog").hide().modal({
            backdrop: true,
            keyboard: true,
            show: false
        });

        this.$svg.attr("width", '100%').attr("height", '100%')

        /*
        var $stats = this.$(".tree-stats")
        if (this.isClassificationTree) {

            var $tbody = $("<tbody />")

            var symbols = this.result.tree.symbols
            for (var i = 0; i < symbols.length; i++) {
                var symbol = symbols[i]
                $("<tr/>").append($("<td/>").text(symbol).css("background-color", this.colorMap.getColor(symbol))).appendTo($tbody)
            }

            $stats.append($('<table class="graphics-stats" border="1" cellpadding="5" cellspacing="0"><thead><tr><th>Symbols</th><tr></thead></table>').append($tbody))

            var treeStats = this.result.stats[this.result.tree.root.id]
            $stats.append($("<br/>"))
            $stats.append($("<b>Learning set success rate:</b>"))
            $stats.append(document.createTextNode(" " + formatNumber(treeStats.successCount / treeStats.count, 6) * 100 + "%"))

            if (this.result.testStats) {
                $stats.append($("<br>"))
                var testStats = this.result.testStats[this.result.tree.root.id]
                $stats.append($("<b>Test set success rate:</b>"))
                $stats.append(document.createTextNode(" " + formatNumber(testStats.successCount / testStats.count, 6) * 100 + "%"))
            }
        }

        this.$(".reset-btn").click(_.bind(this.drawTree, this))
        this.$(".zoom-in-btn").click(_.bind(this.zoomIn, this))
        this.$(".zoom-out-btn").click(_.bind(this.zoomOut, this))

        */

    },
    onResize: function () {
        this.$svg.attr("width", this.$(".tree-graph").width())
        this.$svg.attr("height", this.$(".tree-graph").height())
//			this.drawTree();
    },
    drawTree: function () {
        this.$svg.empty()

        var treeLayout = new TreeLayout()
        treeLayout.getNodeChildren = function (node) {
            if (node.trueChild) {
                return [node.trueChild, node.falseChild]
            } else {
                return null
            }
        }

        this.boxWidth = treeLayout.defaultWidth
        var bounds = treeLayout.layout(this.root)

        var svgNode = this.createSVGNode(this.root)

        this.$svg.append(svg("g", {style: "font:10px sans-serif"}, svgNode))

        if (this.mouseTool) {
            this.mouseTool.unbind()
        }

        this.mouseTool = new SVGMouseTool(this.$svg, this.$svg.find("g").first())
        // margin
        var margin = 20
        bounds.offsetX -= margin
        bounds.offsetY -= margin
        bounds.width += 2 * margin
        bounds.height += 2 * margin
        this.mouseTool.fitToBounds(bounds)
        this.mouseTool.bind(this.$svg)

    },
    fillTreeNodeStats: function (node, treeStats) {
        var nodeStats
        switch (node.type) {
            case "thresholdTest":
            case "subsetTest":
                nodeStats = {}
                treeStats[node.id] = nodeStats
                var trueStats = this.fillTreeNodeStats(node.trueChild, treeStats)
                var falseStats = this.fillTreeNodeStats(node.falseChild, treeStats)

                nodeStats.count = trueStats.count + falseStats.count

                if (this.isClassificationTree) {
                    nodeStats.counts = []
                    for (var i = 0; i < falseStats.counts.length; i++) {
                        nodeStats.counts[i] = falseStats.counts[i] + trueStats.counts[i]
                    }

                    if (trueStats.count) {
                        if (falseStats.count) {
                            nodeStats.successCount = trueStats.successCount + falseStats.successCount
                        } else {
                            nodeStats.successCount = trueStats.successCount
                        }
                    } else {
                        if (falseStats.count) {
                            nodeStats.successCount = falseStats.successCount
                        } else {
                            nodeStats.successCount = 0
                        }
                    }

                } else {
                    if (nodeStats.count) {
                        if (trueStats.count) {
                            if (falseStats.count) {
                                var p1 = trueStats.count / nodeStats.count
                                var mu1 = trueStats.mu
                                var var1 = Math.pow(trueStats.sigma, 2)
                                var p2 = falseStats.count / nodeStats.count
                                var mu2 = falseStats.mu
                                var var2 = Math.pow(falseStats.sigma, 2)
                                nodeStats.mu = mu1 * p1 + mu2 * p2
                                nodeStats.sigma = Math.sqrt(var1 * p1 + var2 * p2 + p1 * p2 * Math.pow((mu2 - mu1), 2))
                                nodeStats.min = Math.min(trueStats.min, falseStats.min)
                                nodeStats.max = Math.max(trueStats.max, falseStats.max)
                            } else {
                                nodeStats.mu = trueStats.mu
                                nodeStats.sigma = trueStats.sigma
                                nodeStats.min = trueStats.min
                                nodeStats.max = trueStats.max
                            }
                        } else {
                            if (falseStats.count) {
                                nodeStats.mu = falseStats.mu
                                nodeStats.sigma = falseStats.sigma
                                nodeStats.min = falseStats.min
                                nodeStats.max = falseStats.max
                            }
                        }
                    }
                }
                break;
            case "leaf":
            default:
                nodeStats = treeStats[node.id]
                if (!nodeStats) {
                    if (this.isClassificationTree) {
                        nodeStats = {counts: []}
                        for (var i = 0; i < this.result.tree.symbols.length; i++) {
                            nodeStats.counts[i] = 0
                        }
                    } else {
                        nodeStats = {count: 0, mu: 0, sigma: 0}
                    }
                    treeStats[node.id] = nodeStats
                }
                if (this.isClassificationTree) {
                    var count = 0
                    for (var i = 0; i < nodeStats.counts.length; i++) {
                        count += nodeStats.counts[i]
                    }
                    nodeStats.count = count

                    var successCount = 0
                    if (nodeStats.count) {
                        for (var i = 0; i < nodeStats.counts.length; i++) {
                            if (this.result.tree.symbols[i] === node.value) {
                                successCount = nodeStats.counts[i]
                                break
                            }
                        }
                    }

                    nodeStats.successCount = successCount

                }
                break;
        }
        return nodeStats
    },

    nodeToPixel: function (node) {
        var xminp = Math.round(node.offsetX) + 0.5
        var yminp = Math.round(node.offsetY) + 0.5
        var xmaxp = Math.round(node.offsetX + node.width) + 0.5
        var ymaxp = Math.round(node.offsetY + node.height) + 0.5
        return {xmin: xminp, ymin: yminp, xmax: xmaxp, ymax: ymaxp}
    },
    createLink: function (x1, y1, x2, y2, style, linkWidth, t) {
        style += ";stroke-width: " + linkWidth + "px"
        var xc = (x1 + x2) / 2.0
        var yc = (y1 + y2) / 2.0
        var g = svg("g", null,
            svg("path", {style: style, d: "M" + x1 + "," + y1 + " C" + x1 + "," + yc + " " + x2 + "," + yc + " " + x2 + "," + y2}),
            svg("circle", {cx: xc, cy: yc, r: 7, fill: "darkgrey"}),
            svg("text", {x: xc, y: yc, fill: "white", "text-anchor": "middle", "dominant-baseline": "central"}, t ? "y" : "n")
        )
        return g
    },
    createSVGNode: function (node, totalCount) {
        var rootStats = this.getNodeStats(this.result.tree.root.id)
        var nodeStats = this.getNodeStats(node.id)
        var style = "stroke-width: 2px;stroke:darkgrey;fill-opacity:0.0"
        var linkStyle = "stroke:darkgrey;fill:none"
        var textStyle = "text-anchor:middle; dominant-baseline:hanging"

        var rect = this.nodeToPixel(node)

        var boxSlices = []
        if (this.isClassificationTree) {
            var x = rect.xmin
            for (var i = 0; i < nodeStats.counts.length; i++) {
                if (nodeStats.counts[i]) {
                    var fillColor = this.colorMap.getColor(this.result.tree.symbols[i])
                    var sliceStyle = "fill:" + fillColor
                    dx = nodeStats.counts[i] / nodeStats.count * (rect.xmax - rect.xmin)
                    boxSlices.push(svg("rect", {x: x, y: rect.ymin, width: dx, height: rect.ymax - rect.ymin, style: sliceStyle}))
                    x += dx
                }
            }

            var failRateHeight = 8
            if (this.result.testStats) {
                var nodeTestStats = this.result.testStats[node.id]
                boxSlices.push(svg("rect", {x: rect.xmin, y: rect.ymax - failRateHeight, width: rect.xmax - rect.xmin, height: failRateHeight, fill: "white"}))
                if (nodeTestStats.count && (nodeTestStats.count > nodeTestStats.successCount)) {
                    failRateWidth = (nodeTestStats.count - nodeTestStats.successCount) / nodeTestStats.count * (rect.xmax - rect.xmin)
                    boxSlices.push(svg("rect", {x: rect.xmax - failRateWidth, y: rect.ymax - failRateHeight, width: failRateWidth, height: failRateHeight, fill: "red"}))
                }
            }

        } else {
            var sliceStyle = "fill:white"
            boxSlices.push(svg("rect", {x: rect.xmin, y: rect.ymin, width: rect.xmax - rect.xmin, height: rect.ymax - rect.ymin, style: sliceStyle}))

            if (nodeStats.count > 0) {

                var ratio = (rootStats.max - rootStats.min > 0) ? (nodeStats.mu - rootStats.min) / (rootStats.max - rootStats.min) : 0.5
                var x = Math.round(rect.xmin + ratio * (rect.xmax - rect.xmin)) + 0.5

                if (nodeStats.sigma) {
                    var sigmaRatio = (rootStats.max - rootStats.min > 0) ? nodeStats.sigma / (rootStats.max - rootStats.min) : 0
                    var dx = Math.round(sigmaRatio * (rect.xmax - rect.xmin))
                    var sigmaBox = svg("rect", {x: x - dx, y: rect.ymin, width: 2 * dx, height: rect.ymax - rect.ymin, fill: "lightblue"})
                    boxSlices.push(sigmaBox)
                }

                var points = x + "," + rect.ymin + " " + x + "," + rect.ymax
                var muLine = svg("polyline", {points: points, stroke: "black"})
                boxSlices.push(muLine)
            }
        }

        var popoverContent = this.getPopoverContent(node)

        var box = svg("rect", {
            className: "svg-popover", x: rect.xmin, y: rect.ymin, width: rect.xmax - rect.xmin, height: rect.ymax - rect.ymin,
            style: style, "data-content": popoverContent.content, "data-original-title": popoverContent.title
        })

        var popContent = svg("text", {style: "fill:white;stroke:none", x: rect.xmax, y: rect.ymin},
            svg("tspan", {dx: "0em", dy: "1em"}, "test"),
            svg("tspan", {dx: "0em", dy: "2em"}, "test2"))

        $(box).svgPopover();

        var testLabel = this.getNodeTestLabel(node)
        var labelWidth = 200

        if (testLabel) {
            //var testLabelNode = svg('text', {x: (rect.xmin + rect.xmax) / 2.0, y: rect.ymax + 5, style: textStyle}, testLabel)
            var testLabelNode = svg(
                'foreignObject',
                {   x: (rect.xmin + rect.xmax) / 2.0 - labelWidth/2,
                    y: rect.ymax + 5,
                    width: labelWidth,
                    height: 25,
                    style: textStyle},
                $('<p class="treeLabel">'+testLabel+'</p>')[0])
        }

        var label = this.getNodeLabel(node)
        if (label) {
            var labelNode = svg('text', {x: (rect.xmin + rect.xmax) / 2.0, y: (rect.ymin + rect.ymax) / 2.0, style: textStyle}, label)
        }

        if (node.trueChild) {
            var trueNode = this.createSVGNode(node.trueChild)
            var falseNode = this.createSVGNode(node.falseChild)

            var yc = rect.ymax

            var leftRect = this.nodeToPixel(node.trueChild)
            var leftxc = (leftRect.xmin + leftRect.xmax) / 2.0
            var leftyc = leftRect.ymin

            var rightRect = this.nodeToPixel(node.falseChild)
            var rightxc = (rightRect.xmin + rightRect.xmax) / 2.0
            var rightyc = rightRect.ymin

            var leftWidth = Math.max(1, this.getNodeStats(node.trueChild.id).count / rootStats.count * this.boxWidth / 2.0)
            var rightWidth = Math.max(1, this.getNodeStats(node.falseChild.id).count / rootStats.count * this.boxWidth / 2.0)
            var totalWidth = leftWidth + rightWidth

            var xc = (rect.xmin + rect.xmax - totalWidth + leftWidth ) / 2.0
            var leftLink = this.createLink(xc, yc, leftxc, leftyc, linkStyle, leftWidth, true)

            var xc = (rect.xmin + rect.xmax + totalWidth - rightWidth ) / 2.0
            var rightLink = this.createLink(xc, yc, rightxc, rightyc, linkStyle, rightWidth, false)

        }

        return svg("g", {}, leftLink, rightLink, boxSlices, labelNode, box, trueNode, falseNode, testLabelNode)
    },
    getNodeLabel: function (node) {
        var nodeStats = this.getNodeStats(node.id)
        var label
        if (this.isClassificationTree) {
        } else {
            if (nodeStats.count > 0) {
                label = formatNumber(nodeStats.mu, 6) + " (" + formatNumber(nodeStats.sigma, 6) + ")"
            }
        }
        return label
    },
    getNodeTestLabel: function (node) {
        var label
        var tree = this.result.tree
        switch (node.type) {
            case "subsetTest":
                label = tree.attributes[node.inputIndex] + " in (" + node.members + ")"
                break;
            case "thresholdTest":
                label = tree.attributes[node.inputIndex] + " < " + node.threshold
                break;
        }
        return label
    },
    getPopoverContent: function (node) {
        var nodeStats = this.getNodeStats(node.id)
        var title, content
        switch (node.type) {
            case "subsetTest":
            case "thresholdTest":
                title = this.getNodeTestLabel(node)
                break
            case "leaf":
            default:
                title = "Leaf : " + node.value
                break
        }
        content = "<b>Object count: </b>" + nodeStats.count
        if (nodeStats.count) {
            if (this.isClassificationTree) {
                var maxIndex = this.findMaxCardinality(nodeStats)
                var symbols = this.result.tree.symbols
                content += "<br><b>Majority symbol: </b>" + symbols[maxIndex]
                content += "<br><b>Succes rate: </b>" + formatNumber(nodeStats.successCount / nodeStats.count * 100, 6) + "%"
                content += "<br><b>Distribution </b>"
                for (var i = 0; i < symbols.length; i++) {
                    if (nodeStats.counts[i]) {
                        content += "<br>" + symbols[i] + ": " + nodeStats.counts[i]
                    }
                }
            } else {
                content += "<br><b>Mu: </b>" + nodeStats.mu
                content += "<br><b>Sigma: </b>" + nodeStats.sigma
                content += "<br><b>Min: </b>" + nodeStats.min
                content += "<br><b>Max: </b>" + nodeStats.max
            }
        }
        return {title: title, content: content}
    },
    getNodeStats: function (nodeId) {
        return this.result.stats[nodeId]
    },
    editNode: function (node, $box) {
        $box && $box.svgPopover("hide")
        var $dialog = $(".tree-node-edit-dialog")
        $dialog.modal("show")
    },
    node2Expression: function (node, level) {
        function quoteString(s) {
            return '"' + s.replace('"', '\\"') + '"'
        }

        function quoteStringArray(as) {
            return _.map(as, function (s) {
                return quoteString(s)
            })
        }

        function testExpr(cond, trueExpr, falseExpr, level) {
            return "(if " + cond + "\n" + indent(level + 1) + trueExpr + "\n" + indent(level + 1) + falseExpr + ")"
        }

        function indent(n) {
            var s = ""
            for (var i = 0; i < n; i++) {
                s += "   "
            }
            return s
        }

        var expr
        if (!node.type || node.type === "leaf") {
            if (this.isClassificationTree) {
                expr = quoteString(node.value)
            } else {
                expr = node.value
            }
        } else {
            if (node.type === "subsetTest") {
                var cond = "(member " + this.result.tree.attributes[node.inputIndex] + " '(" + quoteStringArray(node.members).join(" ") + ") :test #'string=)"
            } else {
                var cond = "(< " + this.result.tree.attributes[node.inputIndex] + " " + node.threshold + ")"
            }
            expr = testExpr(cond, this.node2Expression(node.trueChild, level + 1), this.node2Expression(node.falseChild, level + 1), level)
        }
        return expr
    },
    export: function (evt) {
        evt.preventDefault()
        var expr = this.node2Expression(this.result.tree.root, 0)
        var attributeType = this.isClassificationTree ? "string" : "double-float"
        var attributeName = "DT_EXPORT_" + this.model.get("properties").outputAttributeName
        var props = {body: expr, attributeName: attributeName, attributeType: attributeType}
        appRouter.getProjectView().newTask(this.model.collection.project.id, "function-attribute", props, [
            {name: this.model.findDatasource().id, index: 0}
        ]);
    },
    svgToDataURL: function ($svg) {
        return "data:image/svg+xml;base64,\n" + Base64.encode(this.svgToString($svg))
    },
    svgToString: function ($svg) {
        $svg.attr({ version: '1.1', xmlns: "http://www.w3.org/2000/svg"})
        var $s = $svg.clone()
        $s.find("rect").removeAttr("data-original-title").removeAttr("data-content") // FF doesn't encode the data-* attributes
        var $div = $("<div/>").append($s)
        return $div.html()
    },
    svgToBlob: function ($svg) {
        return new Blob([this.svgToString($svg)], {type: "image/svg+xml"});
    },
    exportSVG: function (ev) {
        ev.preventDefault()
    },
    exportPDF: function (ev) {
        ev.preventDefault()
        var $svg = this.$(".tree-graph").find("svg").first()
        var opts = {
            data: {
                format: "application/pdf",
                svg: this.svgToString($svg)
            },
            httpMethod: "POST",
            successCallback: function (url) {
            },
            failCallback: function (html, url) {

                alert('Error : \r\n' + html);
            }
        }
        $.fileDownload('/svg-convert', opts);
    },
    zoomOut: function () {
        this.mouseTool.zoomByFactor(.6180339887)
    },
    zoomIn: function () {
        this.mouseTool.zoomByFactor(1.6180339887)
    },

    findMaxCardinality: function (nodeStats) {
        var maxIndex = 0
        var maxCount = 0
        for (var i = 0; i < nodeStats.counts.length; i++) {
            if (nodeStats.counts[i] > maxCount) {
                maxCount = nodeStats.counts[i]
                maxIndex = i
            }
        }
        return maxIndex
    }
})

TreeLayout = Class.extend({
    defaultWidth: 50,
    defaultHeight: 35,
    defaultXPadding: 50,
    defaultYPadding: 60,
    getNodeWidth: function (node) {
        return this.defaultWidth
    },
    getNodeHeight: function (node) {
        return this.defaultHeight
    },
    getNodeChildren: function (node) {
        return null
    },
    xArrangeNode: function (node) {
        node.width = this.getNodeWidth(node)
        var children = this.getNodeChildren(node)
        if (children && children.length > 0) {
            var leftBranch = this.xArrangeNode(children[0])
            for (var i = 1; i < children.length; i++) {
                var rightBranch = this.xArrangeNode(children[i])
                var gap = this.findBranchGap(leftBranch, rightBranch)
                this.moveNode(children[i], this.defaultXPadding - gap)
                this.moveBranch(rightBranch, this.defaultXPadding - gap)

                leftBranch = this.mergeBranches(leftBranch, rightBranch)
            }

            var center = (children[0].offsetX + children[0].width / 2.0 + children[children.length - 1].offsetX + children[children.length - 1].width / 2.0) / 2.0
            node.offsetX = center - node.width / 2.0

            leftBranch.unshift({offsetX: node.offsetX, width: node.width})
            return leftBranch
        } else {
            node.offsetX = 0
            return [
                {offsetX: node.offsetX, width: node.width}
            ]
        }
    },
    fillLevelHeights: function (node, heights, level) {
        node.height = this.getNodeHeight(node)
        heights[level] = (level < heights.level) ? Math.max(node.height, heights[level]) : node.height
        var children = this.getNodeChildren(node)
        if (children) {
            for (var i = 0; i < children.length; i++) {
                this.fillLevelHeights(children[i], heights, level + 1)
            }
        }
    },
    yArrangeNode: function (node) {
        var heights = []
        this.fillLevelHeights(node, heights, 0)
        var bottom = this.yMoveNode(node, 0, heights, 0)
        return {offsetY: 0, height: bottom}
    },
    yMoveNode: function (node, offsetY, heights, level) {
        node.offsetY = offsetY
        var bottom = offsetY + heights[level]
        var children = this.getNodeChildren(node)

        if (children && children.length > 0) {
            var leftBottom = this.yMoveNode(children[0], bottom + this.defaultYPadding, heights, level + 1)
            for (var i = 1; i < children.length; i++) {
                var rightBottom = this.yMoveNode(children[i], bottom + this.defaultYPadding, heights, level + 1)
                leftBottom = Math.max(leftBottom, rightBottom)
            }
            return leftBottom
        } else {
            return bottom
        }
    },
    findBranchGap: function (left, right) {
        var minGap = Number.POSITIVE_INFINITY
        for (var i = 0; i < left.length && i < right.length; i++) {
            var gap = right[i].offsetX - (left[i].offsetX + left[i].width)
            if (gap < minGap) {
                minGap = gap
            }
        }
        return minGap
    },
    moveNode: function (node, offset) {
        node.offsetX += offset
        var children = this.getNodeChildren(node)
        if (children) {
            for (var i = 0; i < children.length; i++) {
                this.moveNode(children[i], offset)
            }
        }
    },
    moveBranch: function (branch, offset) {
        for (var i = 0; i < branch.length; i++) {
            branch[i].offsetX += offset
        }
    },
    mergeBranches: function (left, right) {
        var branch = []
        // merge branches
        for (var i = 0; i < left.length || i < right.length; i++) {
            var offset = (i < left.length) ? left[i].offsetX : right[i].offsetX
            var width = (i < right.length) ? (right[i].offsetX + right[i].width - offset) : left[i].width
            branch.push({offsetX: offset, width: width})
        }
        return branch;
    },

    getBranchBounds: function (branch) {
        var max = Number.NEGATIVE_INFINITY
        var min = Number.POSITIVE_INFINITY
        for (var i = 0; i < branch.length; i++) {
            if (branch[i].offsetX < min) {
                min = branch[i].offsetX
            }
            var z = branch[i].offsetX + branch[i].width
            if (z > max) {
                max = z
            }
        }
        return {offsetX: min, width: max - min}
    },

    layout: function (root) {
        var branch = this.xArrangeNode(root)
        var xbounds = this.getBranchBounds(branch)
        var ybounds = this.yArrangeNode(root)
        return {offsetX: xbounds.offsetX, offsetY: ybounds.offsetY, width: xbounds.width, height: ybounds.height}
    }
})

