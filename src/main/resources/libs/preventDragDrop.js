window.ondragover = function(e) { e.preventDefault(); return false };
 window.ondrop = function(e) { e.preventDefault(); return false };
document.addEventListener('dragover', function(e){
  e.preventDefault();
  e.stopPropagation();
}, false);
document.addEventListener('drop', function(e){
  e.preventDefault();
  e.stopPropagation();
}, false);
document.addEventListener('dragleave', function(e){
  e.preventDefault();
  e.stopPropagation();
}, false);

window.ondragleave = function(e) { e.preventDefault(); return false }
