document.observe('keydown', function(e){
    var code = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;
    // Observer for send feedback by Enter keypress. If 'comment' textarea is focused - nothing happens
    if (code == 13 && (Feedback.comment_is_focused == false)){
        Feedback.submit_form();
    }
    // Observer for close feedback by ESC keypress
    if (code == 27 && $('feedback')){
        var comment = $('feedback_comment').value;
        if (comment != 'Type your comment here' && comment != '' && comment != ' '){
            if (confirm("Are you sure you want to close feedback window?") == true) {
                $('feedback_block').update('');
            }
        }
        else {
            $('feedback_block').update('');
        }
    }
});

Feedback = {
//    open_from_maps: function(){
//        ShowDialogDOM("feedback_content", 390);
//        $("feedback_link").blur();
//    },
    set_comment_focus: function(){
        this.comment_is_focused = true;
    },
    remove_comment_focus: function(){
        this.comment_is_focused = false;
    },
    clear_textarea: function(){
        if ($('feedback_note')) $('feedback_note').hide();
        if ($F('feedback_comment') == 'Type your comment here') $('feedback_comment').value = '';
        $('feedback_comment').addClassName('active');
        $('feedback_comment').focus();
        this.set_comment_focus();
    },
    submit_form: function(){
        try {
            if ($$(".wml-permalink a")[0]) $("from_url").value =  $$(".wml-permalink a")[0];
            else $("from_url").value = window.location.href;
            $("browser").value = navigator.userAgent;
            document.form_feedback.onsubmit();
        }
        catch(e){}
    }
}

