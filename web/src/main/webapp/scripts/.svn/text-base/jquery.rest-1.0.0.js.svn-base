function _ajax_request(url, data, callback, dataType, method, contentType) {
    if (jQuery.isFunction(data)) {
        callback = data;
        data = {};
    }
    return jQuery.ajax({
        type: method,
        url: url,
        data: data,
        success: callback,
        dataType: dataType,
        contentType: contentType
        });
}

jQuery.extend({
    put: function(url, data, callback, dataType) {
        return _ajax_request(url, data, callback, dataType, 'PUT', 'application/x-www-form-urlencoded');
    },
    delete_: function(url, data, callback, dataType) {
        return _ajax_request(url, data, callback, dataType, 'DELETE', 'application/x-www-form-urlencoded');
    },
    deleteJSON: function(url, data, callback) {
        return _ajax_request(url, data, callback, 'json', 'DELETE', 'application/json')
    },
    postJSON: function(url, data, callback) {
        return _ajax_request(url, data, callback, 'json', 'POST', 'application/json')
    },
    putJSON: function(url, data, callback) {
        return _ajax_request(url, data, callback, 'json', 'PUT', 'application/json')
    }

});
