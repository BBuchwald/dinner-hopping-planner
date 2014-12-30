/**
 * @author Victoria Moshanova
 */

document.observe("dom:loaded", function(){
    hightlight_error();
})
var hightlight_error = function(){
    if ($$('.input-holder .error').length > 0) {
        var errors_array = $$('.input-holder .error');
        errors_array.each(function(error){
            
            var focused = false;
            if (error.visible()){
                error.observe('mouseover', function(){
                    this.hide();
                });
                var input = error.previous("input") || error.previous("textarea") || error.previous(".jstEditor").down("textarea");
                input.observe('keydown', function(){
                    this.hide();
                }.bind(error));
            }
            
        });
    }
}