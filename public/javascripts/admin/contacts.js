$(document).ready(function(){
	
//	$( "#classification_field" ).autocomplete({
//		source: jsRoutes.controllers.Helper.filterClassifications().url,
//		minLength: 2,
//		select: function( event, ui ) {
//			$('#classification_field').val( ui.item.label );
//		}
//	});
//	$('#classification_field_add').click(function(){
//		$.ajax({
//			url: jsRoutes.controllers.Contacts.probeClassification().url,
//			type: 'POST',
//			data: {name: $('#classification_field').val() },
//			dataType: 'json',
//			success: function(dat) {
//				var size = $('.classification_list li').length;
//				$('.classification_list').append('<li ><input type="hidden" value="'+dat.class_id+'" name="classifications_l['+size+'].id"/><a rel="#" id="'+dat.class_id+'" href="#">'+dat.name+'<span class="delete"></span></a></li>');
//				$('#classification_field').val('')
//				contactClassifictionEvents();
//			}
//		})
//	})
//	contactClassifictionEvents();
	
//	$('#new_tag').click(function(){
//		var randomInt = Math.floor( Math.random() * 9999 );
//		$('#tags_table').append('<tr>'+
//								'	<td><input type="hidden" value="-'+randomInt+'" name="new_tags_seq[-'+randomInt+']"/><input type="text" class="text small" name="new_tags[-'+randomInt+'].tagName" value=""/></td>'+
//								'	<td><input type="text" class="text small" name="new_tags[-'+randomInt+'].tagValue" value=""/></td>'+
//								'	<td><a href=""><img src="/assets/images/admin/error.gif"/></a></td>'+
//								'</tr>');
//		removeTagValue();
//		return false;
//	})
//	removeTagValue();
	$('#add_more_number').click(function(){
		var randomInt = Math.floor( Math.random() * 9999 );
		$('p.phone:last').after('<p class="phone"> <input type="hidden" value="-'+randomInt+'" name="new_tags_seq[-'+randomInt+']"/> <input type="hidden" name="new_tags[-'+randomInt+'].type" value="1"/>'+
								' <input type="text" class="text small" name="new_tags[-'+randomInt+'].tagName" value="Phone"/>'+
								' <input type="text" class="text small" name="new_tags[-'+randomInt+'].tagValue" value=""/>'+
								' <a href="#" class="remove_phone"><img src="/assets/images/admin/error.gif"/></a>'+
								'</p>');
		$('p.phone a.remove_phone').unbind('click').click(function(){
			$(this).parents('p:first').remove();
			return false;
		})
		return false;
	})
	
	$('#address_types').autocomplete({
		source: jsRoutes.controllers.Helper.addressesTypes().url,
		minLength: 2,
		select: function( event, ui ) {
			$('#address_types').val( ui.item.label );
		}
	})
	
	$('#link_types').autocomplete({
		source: jsRoutes.controllers.Helper.relationTypes().url,
		minLength: 2,
		select: function( event, ui ) {
			$('#address_types').val( ui.item.label );
		}
	})
	
	$('#contact_relation_name').autocomplete({
		source: jsRoutes.controllers.Contacts.filterByNameJson().url,
		minLength: 2,
		select: function( event, ui ) {
			$('#contact_relation_name').val( ui.item.label );
			$('#link_id').val( ui.item.id );
		},
		open: function(event, ui) {
			$('#link_id').val('');
		}
	})
	
	$('.delete_address, .delete_search, .delete_relation, .delete_note').click(function(){
		var url = $(this).attr('rel');
		var id = $(this).attr('title');
		var that = $(this).parents('tr:first');
		$.ajax({
			data: {id: id},
			dataType: 'json',
			type: 'POST',
			url: url,
			success: function(data){
				$(that).remove();
			}
		})
		return false;
	})
	
})

//function contactClassifictionEvents() {
//	$('.classification_list li a span.delete').unbind('click').click(function(){
//		$(this).parents('li:first').remove();
//		return false;
//	});
//}
//function removeTagValue() {
//	$('.remove_tag_val').unbind('click').click(function(){
//		$(this).parents('tr:first').remove();
//		return false;
//	})
//}