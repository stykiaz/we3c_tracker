$(document).ready(function(){
	
	$('#new_tag').click(function(){
		var randomInt = Math.floor( Math.random() * 9999 );
		$('#tags_table').append('<tr>'+
								'	<td><input type="hidden" value="-'+randomInt+'" name="new_tags_seq[-'+randomInt+']"/><input type="text" class="text small" name="new_tags[-'+randomInt+'].tagName" value=""/></td>'+
								'	<td><input type="text" class="text small" name="new_tags[-'+randomInt+'].tagValue" value=""/></td>'+
								'	<td><a href=""><img src="/assets/images/admin/error.gif"/></a></td>'+
								'</tr>');
		removeTagValue();
		return false;
	})
	removeTagValue();
	
	$('#company_status').autocomplete({
		source: jsRoutes.controllers.Helper.companyStatuses().url,
		minLength: 2,
		select: function( event, ui ) {
			$('#company_status').val( ui.item.label );
		}
	})
	
	$('.delete_note,.delete_quote,.delete_contract').click(function(){
		var url = $(this).attr('href');
		var id = $(this).attr('rel');
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
	
	
	$('table td.icons a.icon.primary').click(function(){
		var id = $(this).attr('rel');
		var that = this;
		$.ajax({
			url: jsRoutes.controllers.Contacts.makePrimary().url,
			type: 'POST',
			data: { id: id },
			dataType: 'json',
			success: function( dat ){
				if( dat.ret_code != 0 ) {
					alert( dat.message );
					return;
				}
				$('table td.icons a.icon.primary').removeClass('active');
				$(that).addClass('active');
				$('input[name="primaryContactName"]').val( dat.contact_name );
				$('input[name="primaryContactPosition"]').val( dat.contact_position );
				$('input[name="primaryContactPhone"]').val( dat.contact_phone );
			}
		})
		return false;
	})
	$('table td.icons a.icon.authorised').click(function(){
		var id = $(this).attr('rel');
		var that = this;
		$.ajax({
			url: ( $(that).hasClass('active')?jsRoutes.controllers.Contacts.makeUnauthorised().url : jsRoutes.controllers.Contacts.makeAuthorised().url ),
			type: 'POST',
			data: { id: id },
			dataType: 'json',
			success: function( dat ){
				if( dat.ret_code != 0 ) {
					alert( dat.message );
					return;
				}
				
				$(that).toggleClass('active');
			}
		})
		return false;
	})
	$('table td.icons a.icon.registered_addr').click(function(){
		var id = $(this).attr('rel');
		var that = this;
		$.ajax({
			url: jsRoutes.controllers.Helper.getAddress(id).url ,
			type: 'GET',
			dataType: 'json',
			success: function( dat ){
				if( dat.ret_code != 0 ) {
					alert( dat.message );
					return;
				}
				for(var key in dat) {
					$('input[name="registered'+key+'"]').val( dat[key] );
				}
			}
		})
		return false;
	})
	$('table td.icons a.icon.billing_addr').click(function(){
		var id = $(this).attr('rel');
		var that = this;
		$.ajax({
			url: jsRoutes.controllers.Helper.getAddress(id).url ,
			type: 'GET',
			dataType: 'json',
			success: function( dat ){
				if( dat.ret_code != 0 ) {
					alert( dat.message );
					return;
				}
				for(var key in dat) {
					$('input[name="billing'+key+'"]').val( dat[key] );
				}
			}
		})
		return false;
	})
	
})

function removeTagValue() {
	$('.remove_tag_val').unbind('click').click(function(){
		$(this).parents('tr:first').remove();
		return false;
	})
}