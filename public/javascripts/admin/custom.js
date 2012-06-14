$(function () {
	
	// Preload images
//	$.preloadCssImages();
	
	// CSS tweaks
	$('#header #nav li:last').addClass('nobg');
	$('.block_head ul').each(function() { $('li:first', this).addClass('nobg'); });
	$('.block form input[type=file]').addClass('file');
	
	// Web stats
//	$('table.stats').each(function() {
//		
//		if($(this).attr('rel')) {
//			var statsType = $(this).attr('rel');
//		} else {
//			var statsType = 'area';
//		}
//		
//		var chart_width = ($(this).parent('div').width()) - 60;
//		
//				
//		if(statsType == 'line' || statsType == 'pie') {		
//			$(this).hide().visualize({
//				type: statsType,	// 'bar', 'area', 'pie', 'line'
//				width: chart_width,
//				height: '240px',
//				colors: ['#6fb9e8', '#ec8526', '#9dc453', '#ddd74c'],
//				
//				lineDots: 'double',
//				interaction: true,
//				multiHover: 5,
//				tooltip: true,
//				tooltiphtml: function(data) {
//					var html ='';
//					for(var i=0; i<data.point.length; i++){
//						html += '<p class="chart_tooltip"><strong>'+data.point[i].value+'</strong> '+data.point[i].yLabels[0]+'</p>';
//					}	
//					return html;
//				}
//			});
//		} else {
//			$(this).hide().visualize({
//				type: statsType,	// 'bar', 'area', 'pie', 'line'
//				width: chart_width,
//				height: '240px',
//				colors: ['#6fb9e8', '#ec8526', '#9dc453', '#ddd74c']
//			});
//		}
//	});
	
	// Sort table
//	$("table.sortable").tablesorter({
//		headers: { 0: { sorter: false}, 5: {sorter: false} },		// Disabled on the 1st and 6th columns
//		widgets: ['zebra']
//	});
	
	$('.block table tr th.header').css('cursor', 'pointer');
	
	// Check / uncheck all checkboxes
	$('.check_all').click(function() {
		$(this).parents('form').find('input:checkbox').attr('checked', $(this).is(':checked'));   
	});
	
	// Set WYSIWYG editor
	//$('.wysiwyg').wysiwyg({css: "/public/stylesheets/admin/jquery.wysiwyg.css", brIE: false });
	
	// Modal boxes - to all links with rel="facebox"
	
	
		if( jQuery.prototype.lightBox ) $('a[rel*=lightbox]').lightBox()
	
	
	// Messages
	$('.block .message').hide().append('<span class="close" title="Dismiss"></span>').fadeIn('slow');
	$('.block .message .close').hover(
		function() { $(this).addClass('hover'); },
		function() { $(this).removeClass('hover'); }
	);
		
	$('.block .message .close').click(function() {
		$(this).parent().fadeOut('slow', function() { $(this).remove(); });
	});
	
	// Form select styling
	if( jQuery.prototype.select_skin ) $("form select.styled").select_skin();
	
	// Tabs
	$(".tab_content").hide();
	$("ul.tabs li:first-child").addClass("active").show();
	$(".block").find(".tab_content:first").show();

	$("ul.tabs li").click(function() {
		$(this).parent().find('li').removeClass("active");
		$(this).addClass("active");
		$(this).parents('.block').find(".tab_content").hide();
			
		var activeTab = $(this).find("a").attr("href");
		$(activeTab).show();
		
		// refresh visualize for IE
		$(activeTab).find('.visualize').trigger('visualizeRefresh');
		
		return false;
	});
	
	// Sidebar Tabs
	$(".sidebar_content").hide();
	if(window.location.hash && window.location.hash.match('sb')) {
		$("ul.sidemenu li a[href="+window.location.hash+"]").parent().addClass("active").show();
		$("ul.sidemenu li a[rel="+window.location.hash.replace('#', '')+"]").parent().addClass("active").show();
		$(".block .sidebar_content#"+window.location.hash).show();
		window.scrollTo(0, 0);
	} else {
		if( !$("ul.sidemenu li:first-child a.direct").length > 0 ) $("ul.sidemenu li:first-child").addClass("active").show();
		$(".block .sidebar_content:first").show();
	}

	$("ul.sidemenu li").click(function() {
		var linkEl = $(this).find("a");
		if( linkEl.hasClass("direct") ) return true;
		var activeTab = linkEl.attr("href");
		window.location.hash = activeTab;
	
		$(this).parent().find('li').removeClass("active");
		$(this).addClass("active");
		$(this).parents('.block').find(".sidebar_content").hide();			
		$(activeTab).show();
		return false;
	});	
	
	// Block search
	$('.block .block_head form .text').bind('click', function() { $(this).attr('value', ''); });
	
	// Image actions menu
	$('ul.imglist li').hover(
		function() { $(this).find('ul').css('display', 'none').fadeIn('fast').css('display', 'block'); },
		function() { $(this).find('ul').fadeOut(100); }
	);
		
	// Image delete confirmation
	$('ul.imglist .delete a').click(function() {
		if (confirm("Are you sure you want to delete this image?")) {
			var url = $(this).attr('rel');
			var id = $(this).attr('name');
			$.ajax({
				data: {id: id},
				dataType: 'json',
				type: 'POST',
				url: url,
				success: function(data){
					$('li#image_'+data.id).remove();
				}
			})
			return false;
		} else {
			return false;
		}
	});
	// Image delete confirmation
	$('ul.imglist .preferred a').click(function() {
		var url = $(this).attr('rel');
		var id = $(this).attr('name');
		$.ajax({
			data: {id: id},
			dataType: 'json',
			type: 'POST',
			url: url,
			success: function(data){
				$('ul.imglist li').removeClass('main');
				$('li#image_'+data.id).addClass("main");
			}
		})
		return false;
	});
	
	// Style file input
	$("input[type=file]").filestyle({ 
	    image: "/assets/images/admin/upload.gif",
	    imageheight : 30,
	    imagewidth : 80,
	    width : 250
	});
	
	// File upload
//	if ($('#fileupload').length) {
//		new AjaxUpload('fileupload', {
//			action: 'upload-handler.php',
//			autoSubmit: true,
//			name: 'userfile',
//			responseType: 'text/html',
//			onSubmit : function(file , ext) {
//					$('.fileupload #uploadmsg').addClass('loading').text('Uploading...');
//					this.disable();	
//				},
//			onComplete : function(file, response) {
//					$('.fileupload #uploadmsg').removeClass('loading').text(response);
//					this.enable();
//				}	
//		});
//	}
	
	// Date picker
	$('input.date_picker').datepicker({ firstDay: 1, dateFormat: 'dd/mm/yy' });

	// Navigation dropdown fix for IE6
	if(jQuery.browser.version.substr(0,1) < 7) {
		$('#header #nav li').hover(
			function() { $(this).addClass('iehover'); },
			function() { $(this).removeClass('iehover'); }
		);
	}
	
	// IE6 PNG fix
	if( jQuery.pngFix ) $(document).pngFix();
	
	/******
	 * BEGIN: Common triggers,acction,events
	 */
	
	$('.toggle_filter').click(function(){
		console.log( '.'+$(this).attr('rel') );
		$('.'+$(this).attr('rel')).toggle();
		return false;
	})
	
	$('.block_content table tbody tr').each(function(i){
		if( i % 2 == 0 ) $(this).addClass('even');
		else $(this).addClass('odd');
	})
	
	
	$('.delete a.delete').click(function(){
		var url = $(this).attr('rel');
		var id = $(this).attr('id');
		$.ajax({
			data: {id: id},
			dataType: 'json',
			type: 'POST',
			url: url,
			success: function(data){
				$('tr#row_'+data.id).remove();
			}
		})
		return false;
	})
	/******
	 * END: Common triggers,acction,events
	 */
	
	/********
	 * BEGIN: Property Features
	 */
	try {
		$( "#feature" ).autocomplete({
			source: featuresSource,
			minLength: 2,
			select: function( event, ui ) {
				$('#feature').val( ui.item.label );
				/*
				log( ui.item ?
					"Selected: " + ui.item.value + " aka " + ui.item.id :
					"Nothing selected, input was " + this.value );
				*/
			}
		});
	} catch(e){}
	try {
		$( "#organisation_filter" ).autocomplete({
			source: organisationsSource,
			minLength: 2,
			select: function( event, ui ) {
				$('#organisation_filter').val( ui.item.label );
				/*
				log( ui.item ?
					"Selected: " + ui.item.value + " aka " + ui.item.id :
					"Nothing selected, input was " + this.value );
				 */
			}
		});
	} catch(e){}
	
	$('ul.labels_list a span.delete').click(function(){
		var url = $(this).parents('a:first').attr('rel');
		var id = $(this).parents('a:first').attr('id');
		var propertyId = $('#property_id').val();
		$.ajax({
			data: {id: id, property_id: propertyId},
			dataType: 'json',
			type: 'POST',
			url: url,
			success: function(data){
				$('li#feature_'+data.id).remove();
			}
		})
		return false;
	})
	/********
	 * END: Property Features
	 */
	
});