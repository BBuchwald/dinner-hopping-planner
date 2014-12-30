SignUp = {
    
    light_form_id: "light-sign-up",
    
    submit_form: function(){
        new Ajax.Request("/enter", {
            method: 'post',
            evalScripts: true,
            parameters: $(this.light_form_id).serialize(true),
            onCreate: function(){
                if (!window.location.href.substr('maps.')) {
                    Element.setStyle($(SignUp.light_form_id), {
                        'opacity': '0.4'
                    })
                }
            },
            onComplete: function(transport){
                if (transport.status == 200){
                    var response = transport.responseText;
                    if(response.include('form')){
                        Element.replace($(SignUp.light_form_id), response);
                    }
                    else {
                        window.location.href = response;
                    }
                }
                else {
                    alert('Something went wrong. Please, try again.');
                    window.location.reload();
                }
            }
        })
    },

    hide_errors: function(){
        var errors_array = $$('#' + SignUp.light_form_id + ' .error');
        errors_array.each(function(error){
            Element.hide(error);
        })
    }
}

