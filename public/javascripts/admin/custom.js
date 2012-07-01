$(function () {
	
	// CSS tweaks
	$('#header #nav li:last').addClass('nobg');
	$('.block_head ul').each(function() { $('li:first', this).addClass('nobg'); });
	$('.block form input[type=file]').addClass('file');
	
	$('.block table tr th.header').css('cursor', 'pointer');
	
	// Check / uncheck all checkboxes
	$('.check_all').click(function() {
		$(this).parents('form').find('input:checkbox').attr('checked', $(this).is(':checked'));   
	});
	
	
	
	// Messages
	$('.block .message').hide().append('<span class="close" title="Dismiss"></span>').fadeIn('slow');
	$('.block .message .close').hover(
		function() { $(this).addClass('hover'); },
		function() { $(this).removeClass('hover'); }
	);
		
	$('.block .message .close').click(function() {
		$(this).parent().fadeOut('slow', function() { $(this).remove(); });
	});
	
	
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
	
	
});