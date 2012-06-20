var we3cPlayback = null;
$('#playback').load(function(){
	var document = $('#playback').contents();
//	console.log( document );
//	console.log( $('body',document ) );
//	alert("1" );
	function playbackCursor( locationId ) {
		this.theIframe = $('#playback');
		this.theDocument = $('#playback').get(0).contentWindow.document;
		this.theContentWindow = $('#playback').get(0).contentWindow;
		this.theBody = $('body', this.theDocument);
		this.locationId = locationId;
		this.data = null;
		this.playBackTimer = null;
		this.currentTimeDuration = 0;
		this.currentTimeDurationElement = $('#current_playback');
		
		this.hoveredELement = null;
		
		this.cursorCodeCode = '<div id="we3ctracker_cursor" '+ 
		 '    style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px; top: 0; left: 0; '+
		 '           background-image: url(\'/assets/images/site/cursors.png\'); background-position: 0 0; transition: all 100ms; -moz-transition: all 100ms; -webkit-transition: all 100ms; -o-transition: all 100ms;"></div>';
		this.clickCodeCode = '<div id="we3ctracker_click_{id}" '+ 
		'    style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px; top: {top}px; left: {left}px; '+
		'           background-image: url(\'/assets/images/site/cursors.png\'); background-position: 0 -64px;"></div>';
		this.cursorElement = null
		this.initCode = function() {
			this.theBody.append( this.cursorCodeCode );
			this.cursorElement = $('#we3ctracker_cursor', this.theBody);
			var that = this;
			this.loadData();
		}
		this.loadData = function() {
			var that = this;
			$.getJSON( jsRoutes.controllers.Preview.getData( this.locationId ).url, function(data){
				that.data = data;
				that.playBack();
			} )
		}
		
		this.putClick = function( position ) {
			var id = position.ts;
			var clickCode = this.clickCodeCode.replace("{id}", id).replace("{top}", position.y).replace("{left}", position.x);
			this.theBody.append( clickCode );
			setTimeout( '$("#we3ctracker_click_'+id+'", we3cPlayback.theBody).fadeOut("fast")', 300 );
		}
		
		this.playBack = function() {
//			console.log( this.data[0].e );
			switch( parseInt( this.data[0].e ) ) {
				case 0:
					this.theIframe.css('width', this.data[0].w+"px");
					this.theIframe.css('height', this.data[0].h+"px");
					this.cursorElement.css('top', this.data[0].y+"px");
					this.cursorElement.css('left', this.data[0].x+"px");
					break;
				case 1:
					this.cursorElement.css('top', this.data[0].y+"px");
					this.cursorElement.css('left', this.data[0].x+"px");
					$( this.theDocument.elementFromPoint( parseInt( this.data[0].x ) - 1 , parseInt( this.data[0].y ) - 1 ) ). trigger('click');
					this.putClick(this.data[0]);
					break;
				case 2:
					this.cursorElement.css('top', this.data[0].y+"px");
					this.cursorElement.css('left', this.data[0].x+"px");
					break;
				case 3:
					this.theIframe.css('width', this.data[0].w+"px");
					this.theIframe.css('height', this.data[0].h+"px");
					break;
				case 4:
					this.theContentWindow.scrollTo( this.data[0].l, this.data[0].t );
					break;
			}
			var tsDiff = tsDiffOrig = 0;
			if( this.data.length > 1 ) {
				tsDiffOrig = tsDiff = this.data[1].ts - this.data[0].ts;
				if( tsDiff > 3000 ) tsDiff = 1000;
				if( parseInt( this.data[1].e ) == 1 || parseInt( this.data[1].e ) == 2 ) {
					this.cursorElement.css( 'transition-duration', tsDiff+'ms');
					this.cursorElement.css( '-moz-transition-duration', tsDiff+'ms');
					this.cursorElement.css( '-webkit-transition-duration', tsDiff+'ms');
					this.cursorElement.css( '-o-transition-duration', tsDiff+'ms');
				}
				this.data.shift();
				this.currentTimeDurationElement.text( ( this.currentTimeDuration / 1000 ) +" second");
				this.playBackTimer = setTimeout("we3cPlayback.playBack(); we3cPlayback.currentTimeDuration+="+tsDiffOrig+";", tsDiff);
			} else {
				alert( "The End" );
			}
			
		}
	}
	
	we3cPlayback = new playbackCursor( viewLocationId );
	we3cPlayback.initCode();
	
})