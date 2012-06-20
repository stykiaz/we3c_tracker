function we3cTracker( config ) {
	this.accountApiKey = config.accountId;
	//TODO: load appropriate domain
	this.eventServer = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'clickheat.wethreecreatives.com/v1/track.gif';
	this.location = document.location.href;
	this.eventTypesList = { 'init': 0, 'mousedown': 1, 'mousemove': 2, 'resize': 3, 'scroll': 4, 'locationChange': 5 };
	this.eventsRepo = [];
	this.offloadThreshold = 60;
	this.lastMouseMoveState = [0,0]; //[x,y] coefs
};
//TODO: track html5 popstatechange (History)
we3cTracker.prototype.trackEvent = function(e){
	var state = {'ts': new Date().getTime() };
	switch( e.type ) {
		case 'init':
			_we3ctr.submitSessionStorage();
			state['e'] = _we3ctr.eventTypesList[e.type];
			state['loc'] = document.location.toString();
			var tmp = _we3ctr.getWindowsSize();
			state['w'] = tmp['w'];
			state['h'] = tmp['h'];
			state['t'] = _we3ctr.getScrollTop();//top
			state['l'] = _we3ctr.getScrollLeft();//left
			this.eventsRepo.push(state);
			break;
		case 'unload':
			_we3ctr.storeToSessionStorage();
			return;
		case 'mousedown':
			//track mouse downs only one per second
			state['e'] = _we3ctr.eventTypesList[e.type];
			if( _we3ctr.eventsRepo.length >= 1 && _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['e'] == state['e'] && state['ts'] - _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['ts'] < 1000 ) {
				return;
			}
			state['x'] = e.clientX;
			state['y'] = e.clientY;
			var tmp = _we3ctr.getWindowsSize();
			state['w'] = tmp['w'];
			state['h'] = tmp['h'];
			_we3ctr.eventsRepo.push(state);
			break;
		case 'mousemove':
			//track mouse move only if direction has changed!
			state['e'] = _we3ctr.eventTypesList[e.type];
			state['x'] = e.clientX;
			state['y'] = e.clientY;
			var tmp = _we3ctr.getWindowsSize();
			state['w'] = tmp['w'];
			state['h'] = tmp['h'];
			if( _we3ctr.eventsRepo.length >= 1 &&  _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['e'] == state['e'] ) {
//				console.log( ( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['x'] - state['x'] ) % 2, 
//							_we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['y'] - state['y'] );
				var xDiff = (_we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['x'] - state['x']);
				var yDiff = (_we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['y'] - state['y']);
				if( Math.abs( xDiff ) <= 8 && Math.abs( yDiff ) <= 8  ) return;
				var xDelta = 0;
				if( xDiff >= 0 ) xDelta = 1;
				else if( xDiff < 0 ) xDelta = -1;
				var yDelta = 0;
				if( yDiff >= 0 ) yDelta = 1;
				else if( yDiff < 0 ) yDelta = -1;
				var newCoef = [ xDelta, yDelta ];
				if( newCoef[0] == 0 && newCoef[1] == 0 ) return;
				if( newCoef[0] != _we3ctr.lastMouseMoveState[0] || newCoef[1] != _we3ctr.lastMouseMoveState[1] ) {
//					console.log(newCoef, _we3ctr.lastMouseMoveState, '(', state['x'], state['y'], ') (',_we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['x'], _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['y'],')' );
//					console.log(state);
					_we3ctr.eventsRepo.push(state);
					_we3ctr.lastMouseMoveState = newCoef;
				} else {
					_we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ] = state;
				}
			} else {
				_we3ctr.eventsRepo.push(state);
//				console.log(state);
			}
			
			break;
		case 'resize':
			//track only the final state of the resize !
			state['e'] = _we3ctr.eventTypesList[e.type];
			var tmp = _we3ctr.getWindowsSize();
			state['w'] = tmp['w'];
			state['h'] = tmp['h'];
			if( _we3ctr.eventsRepo.length >= 1 && _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['e'] == state['e'] ) _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ] = state;
			else _we3ctr.eventsRepo.push(state);
			break;
		case 'scroll':
			//track only when direction changes!
			state['e'] = _we3ctr.eventTypesList[e.type];
			state['t'] = _we3ctr.getScrollTop();//top
			state['l'] = _we3ctr.getScrollLeft();//left
			if( _we3ctr.eventsRepo.length >= 1 &&  _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['e'] == state['e'] ) {
				if( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['t'] > state['t'] ) state['d'] = 't';//direction = top;
				else if( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['t'] < state['t'] ) state['d'] = 'd';//direction = down;
				else if( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['l'] > state['l'] ) state['d'] = 'l';//direction = left;
				else if( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['l'] < state['l'] ) state['d'] = 'r';//direction = right;
				if( _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ]['d'] == state['d'] ) _we3ctr.eventsRepo[ _we3ctr.eventsRepo.length - 1 ] = state;
				else _we3ctr.eventsRepo.push(state); 
			} else  {
				state['d'] = '';
				_we3ctr.eventsRepo.push(state);
			}
			break;
		default:
			break;
	}
	if( _we3ctr.eventsRepo.length >= _we3ctr.offloadThreshold ) {
		_we3ctr.offloadData();
		_we3ctr.lastMouseMoveState = [0, 0];
	}
//	console.log( state );
	
}
we3cTracker.prototype.storeToSessionStorage = function() {
	if(typeof(Storage)!=="undefined") {
		var stringified = this.repoStringify( this.eventsRepo )
		sessionStorage._we3cTrackerStoredState = stringified; 
	} else { 
		//sorry, no session storage supported, keep up!
	}
	return;
	
}
we3cTracker.prototype.submitSessionStorage = function() {
	if( sessionStorage._we3cTrackerStoredState == null ) return;
	this.submitData( this.base64Encode( sessionStorage._we3cTrackerStoredState ) );
	sessionStorage._we3cTrackerStoredState = null;
}
we3cTracker.prototype.periodicOffload = function() {
//	console.log( this.eventsRepo.length );
//	console.log( this.eventsRepo[ this.eventsRepo.length - 1 ].ts );
//	console.log( new Date().getTime() - this.eventsRepo[ this.eventsRepo.length - 1 ].ts );
	if( this.eventsRepo.length > 0 &&  ( new Date().getTime() - this.eventsRepo[ this.eventsRepo.length - 1 ].ts ) > 10000 ) {
		this.offloadData();
	}
	setTimeout( '_we3ctr.periodicOffload()', 10000 );
}
we3cTracker.prototype.offloadData = function() {
	var currentRepo = this.eventsRepo;
	this.eventsRepo = [];
	var stringified = this.repoStringify( currentRepo );
	this.submitData( this.base64Encode( stringified ) );
}
we3cTracker.prototype.submitData = function(repoString) {
	var datIm = new Image(); 
	datIm.src = this.eventServer+"?d="+repoString+"&host="+document.location.host+"&key="+this.accountApiKey; 
	
}
we3cTracker.prototype.repoStringify = function( repo ) {
	var stringified = "";
	for( var stateKey in repo ) {
		var stateString = "";//"{";
		for(var key in repo[stateKey] ) {
			if( key != 'ts' ) stateString += /*key+":"+*/repo[stateKey][key]+"|";
		}
		stateString += /*"ts:"+*/repo[stateKey]['ts'];
		stringified += stateString+"}";//+"},";
	}
	return /*"["+*/stringified/*.substring(0,stringified.length-1)+"]"*/;
}
we3cTracker.prototype.initTracking = function() {
	this.trackEvent({'type': 'init'});
	this.addEventListener(document, 'mousedown', this.trackEvent);
	this.addEventListener(document, 'mousemove', this.trackEvent);
	this.addEventListener(window, 'resize', this.trackEvent);
	this.addEventListener(window, 'scroll', this.trackEvent);
	this.addEventListener(window, 'unload', this.trackEvent);
}
we3cTracker.prototype.addEventListener = function(obj, evtName, f)
{
	/* FF */
	if (document.addEventListener) {
		if (obj) obj.addEventListener(evtName, f, false);
		else addEventListener(evtName, f, false);
	}
	/* IE */
	else if (attachEvent) {
		if (obj) obj.attachEvent('on' + evtName, f);
		else attachEvent('on' + evtName, f);
	}
}
we3cTracker.prototype.getWindowsSize = function() {
	if (typeof window.innerWidth != 'undefined') {
		return {'w': window.innerWidth, 'h': window.innerHeight};
	}
	// IE6 in standards compliant mode (i.e. with a valid doctype as the first line in the document)
	else if (typeof document.documentElement != 'undefined' 		&& typeof document.documentElement.clientWidth != 'undefined' && document.documentElement.clientWidth != 0) {
		return {'w': document.documentElement.clientWidth, 'h': document.documentElement.clientHeight};
	}
}
we3cTracker.prototype.getScrollLeft = function() {
	if( window.pageXOffset ) return window.pageXOffset;
	else if( document.documentElement ) return document.documentElement.scrollLeft;
	else if( document.body ) return document.body.scrollLeft;
	return 0;
}
we3cTracker.prototype.getScrollTop = function() {
	if( window.pageYOffset ) return window.pageYOffset;
	else if( document.documentElement ) return document.documentElement.scrollTop;
	else if( document.body ) return document.body.scrollTop;
	return 0;
	
}
we3cTracker.prototype.base64Encode = function(data) {
    var b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    var o1, o2, o3, h1, h2, h3, h4, bits, i = 0, ac = 0, enc = "", tmp_arr = [];

    if (!data) return data;

    do { // pack three octets into four hexets
        o1 = data.charCodeAt(i++);
        o2 = data.charCodeAt(i++);
        o3 = data.charCodeAt(i++);

        bits = o1 << 16 | o2 << 8 | o3;

        h1 = bits >> 18 & 0x3f;
        h2 = bits >> 12 & 0x3f;
        h3 = bits >> 6 & 0x3f;
        h4 = bits & 0x3f;

        // use hexets to index into b64, and append result to encoded string
        tmp_arr[ac++] = b64.charAt(h1) + b64.charAt(h2) + b64.charAt(h3) + b64.charAt(h4);
    } while (i < data.length);

    enc = tmp_arr.join('');
    
    var r = data.length % 3;
    return (r ? enc.slice(0, r - 3) : enc) + '==='.slice(r || 3);

}
_we3ctr = new we3cTracker( _we3ctr );
_we3ctr.initTracking();
setTimeout( '_we3ctr.periodicOffload()', 10000 );