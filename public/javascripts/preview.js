$('#playback').load(function(){
//	var document = $('#playback').get(0).document;
	
//	console.log( $('body',document ) );
//	alert("1" );
	function playbackCursor() {
		this.theDocument = $('#playback').get(0).document;
		this.theBody = $('body', this.theBody);
		this.initCode = function() {
			var cursorCode = '<div style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px;"></div>';
		}
	}
	
	var playback = new playbackCursor();
	playback.initCode();
	
})