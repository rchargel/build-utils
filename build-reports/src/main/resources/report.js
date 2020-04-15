var loadTag = function(element, tag) {
    var i = 0, subElements = element.getElementsByTagName(tag);
    for (; i < subElements.length; i++) {
        if (subElements[i]) {
            return subElements[i];
        }
    }
    return null;
};
var loadLevel = function(element, level) {
    var header, a, text, listText = '', i = 0, headers = element.getElementsByTagName("h" + level);
    if (headers.length > 0) {
        listText += '<ol>';
        for (; i < headers.length; i++) {
            header = headers[i];a = loadTag(header, 'a');
            text = header.innerText || header.textContent;
            listText += '<li><a href="#' + a.getAttribute('name') + '">' + text + '</a>';
            if (level < 5) {
                listText += loadLevel(header.parentElement, level + 1);
            } listText += '</li>';
        }
        listText += '</ol>';
    }
    return listText;
};
var toc = document.getElementById('tableofcontents');
toc.innerHTML = loadLevel(document, 2);
var transitionTime=200;
var thumbnailMe = function(element){
    var oh = element.clientHeight;
    var ow = element.clientWidth;
    var sh = 75;
    var sw = 75;
    element.style.height = sh + 'px';
    element.style.width = sw + 'px';
    element.size = 'thumb';
    element.addEventListener('click', function(evt) {
        let el = evt.target;
        var start = null;
        var grow = function(time) {
            if (start==null) {
                start=time;
            }
            let prog = (time - start) / transitionTime;
            let nh = Math.min(oh, (oh - sh) * prog + sh);
            let nw = Math.min(ow, (ow - sw) * prog + sw);
            el.style.height = nh + 'px';
            el.style.width = nw + 'px';
            if (nh < oh || nw < ow) {
                requestAnimationFrame(grow);
            } else {
                el.size='orig';
            }
        };
        var shrink = function(time) {
            if (start == null) {
                start = time;
            }
            let prog = (time - start) / transitionTime;
            let nh = Math.max(sh, oh - (oh - sh) * prog);
            let nw = Math.max(sw, ow - (ow - sw) * prog);
            el.style.height = nh + 'px';
            el.style.width = nw + 'px';
            if (nh > sh || nw > sw) {
                requestAnimationFrame(shrink);
            } else {
                el.size = 'thumb';
            }
        };
        if (el.size === 'thumb'){
            requestAnimationFrame(grow);
        } else {
            requestAnimationFrame(shrink);
        }
    });
};
document.querySelectorAll('.thumbnail').forEach(thumbnailMe);