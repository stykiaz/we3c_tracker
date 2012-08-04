var we3cPlayback = null;

$('#playback').load(function(){
	var document = $('#playback').contents();

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
		///assets/images/site/cursors.png
		this.cursortPngData = 'data:image/png;base64,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';
		
		this.cursorCodeCode = '<div id="we3ctracker_cursor" '+ 
		 '    style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px; top: 0; left: 0; '+
		 '           background-image: url(\''+this.cursortPngData+'\'); background-position: 0 0; transition: all 100ms; -moz-transition: all 100ms; -webkit-transition: all 100ms; -o-transition: all 100ms;"></div>';
		this.clickCodeCode = '<div id="we3ctracker_click_{id}" '+ 
		'    style="display: block; position: fixed; z-index: 9999; width: 32px; height: 32px; top: {top}px; left: {left}px; '+
		'           background-image: url(\''+this.cursortPngData+'\'); background-position: 0 -64px;"></div>';
		this.cursorElement = null
		this.initCode = function() {
			$('a', this.theBody).click(function(){ return false; })
			this.theBody.append( this.cursorCodeCode );
			this.cursorElement = $('#we3ctracker_cursor', this.theBody);
			var that = this;
			this.loadData();
			
			$('.player_link.play_pause').click(function(){
				if( $(this).hasClass('play') ) {
					$('.play_pause').removeClass('play').addClass('pause');
					we3cPlayback.playBack();
				} else if( $(this).hasClass('pause') ) {
					$('.play_pause').removeClass('pause').addClass('play');
					clearTimeout(we3cPlayback.playBackTimer);
					we3cPlayback.playBackTimer = null;
				}
			})
			$('.player_link.reload').click(function(){
				clearTimeout(we3cPlayback.playBackTimer);
				we3cPlayback.playBackTimer = null;
				we3cPlayback.currentTimeDuration = 0;
				we3cPlayback.data = null;
				we3cPlayback.cursorElement.css('top', "0px");
				we3cPlayback.cursorElement.css('left', "0px");
				we3cPlayback.theContentWindow.scrollTo( 0, 0 );
				we3cPlayback.loadData();
			})
			
		}
		this.loadData = function() {
			var that = this;
			try {
				this.data = locationData;
				$('#current_playback').text('0');
				$('.play_pause').removeClass('play').addClass('pause');
				that.playBack();
			} catch( e ) {
				if( e instanceof ReferenceError ) {
					$.getJSON( jsRoutes.controllers.Preview.getData( this.locationId ).url, function(data){
						that.data = data;
						$('#current_playback').text('0');
						$('.play_pause').removeClass('play').addClass('pause');
						that.playBack();
					} );
				}
				
			}
		}
		
		this.putClick = function( position ) {
			var id = position.ts; 
			var clickCode = this.clickCodeCode.replace("{id}", id).replace("{top}", parseInt( position.y ) - 16).replace("{left}", parseInt( position.x ) - 16 );
			this.theBody.append( clickCode );
			setTimeout( '$("#we3ctracker_click_'+id+'", we3cPlayback.theBody).fadeOut("fast")', 300 );
		}
		//playback the action
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

$('#locations').change(function(){
	document.location = jsRoutes.controllers.Preview.view( $(this).val() ).url;
})
