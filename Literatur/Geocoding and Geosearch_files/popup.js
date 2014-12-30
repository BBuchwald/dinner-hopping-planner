var CM_Popup = Class.create({
    id: "",
    url: "",
    close_button: ".close_popup_link",
    width: "",
    height: "",
    parent_node: null,
    bg: "bg",
    loader: "ajax-indicator",
    is_open: false,
    initialize: function(id, url, width, height, parent_node){
        this.id = id;
        this.url = url;
        this.width = (width) ? width : '400';
        this.height = (height) ? height : '170';
        this.parent_node = $(parent_node ? parent_node : document.body);
    },
    open: function(){
        if (!this.is_open) {
            this.is_open = true;
            this.get_DOM();
        }
    },
    close: function(){
        if (this.is_open) {
            this.hide();
            this.destroy_container();
            this.destroy_observers();
            this.is_open = false;
        }
    },
    submit: function(){
        var params = this.submit_params();
        this.hide();
        this.destroy_container();
        this.get_DOM(params);
    },
    keypress_observer: function(e){
        var theCode = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;
        if (theCode == 13) { //Enter pressed
            this.submit();
        }
        if (theCode == 27) { // Esc pressed
            this.close_observer();
        }
    },
    close_observer: function(e){
        this.close();
    },
    init_observers: function(){
        Event.observe(document, 'keypress', this.keypress_observer.bindAsEventListener(this));
        Event.observe(window, 'resize', this.position.bindAsEventListener(this));
        $$(this.close_button).each(function(el){
            el.observe('click', this.close_observer.bindAsEventListener(this));
        }, this);
    },
    destroy_observers: function(){
    //        Event.stopObserving(document.body, 'keypress', this.keypress_oserver.bindAsEventListener(this));
    //        Event.stopObserving(window, 'resize', this.position.bindAsEventListener(this))
    },
    onSuccess: function(response){
        if (!this.is_submit_success(response)) {
            this.build_container();
            this.end_loading();
        }
    },
    onComplete: function(response){
        if (this.is_submit_success(response)) {
            this.success_submit();
            this.end_loading();
        }
        else {
            this.show();
            this.init_observers();
        }
    },
    onFailure: function(){
    },
    is_submit_success: function(response){
        return response.responseText == "success";
    },
    start_loading: function(){
    },
    end_loading: function(){
        $(this.loader).hide();
    },
    get_DOM: function(params){
        this.start_loading();
        new Ajax.Updater(this.id, this.url, {
            parameters: params,
            onSuccess: this.onSuccess.bind(this),
            onComplete: this.onComplete.bind(this),
            onFailure: this.onFailure.bind(this),
            evalScripts: true
        });
    },
    show: function(){
        $(this.bg).show();
        $(this.id).show();
        this.position();
    },
    hide: function(){
        $(this.id).hide();
        $(this.bg).hide();
    },
    build_container: function(){
        this.bg_container = new Element('div', {
            'id': this.bg
        });
        this.parent_node.insert(this.bg_container);
        this.container = new Element('div', {
            'id': this.id
        });
        this.parent_node.insert(this.container);
    },
    destroy_container: function(){
        this.container.remove();
        this.bg_container.remove();
    },
    success_submit: function(){
        this.close();
    },
    submit_params: function(){
        return $$("#" + this.id + " form")[0].serialize();
    },
    position: function(){
        var windowWidth = document.viewport.getWidth();
        var windowHeight = document.viewport.getHeight();
        var popup_left = (windowWidth - this.width)/2;
        var popup_top = (windowHeight - this.height)/2;
        this.bg_container.setStyle({
            position: 'absolute',
            top: '0px',
            left: '0px',
            width: windowWidth + 'px',
            height: windowHeight + 'px'
        });
        this.container.setStyle({
            position: 'absolute',
            top: popup_top + 'px',
            left: popup_left + 'px',
            width: this.width + 'px',
            height: this.height + 'px'
        });
        if (!this.bg_container.hasClassName('popup_bg'))this.bg_container.addClassName('popup_bg');
        if (!this.container.hasClassName('popup'))this.container.addClassName('popup');
    }
});
