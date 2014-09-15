package uk.co.squadlist.web.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;

import com.google.common.collect.Lists;

public class RequestBuilder {

	private static final String UTF8 = "UTF8";
	
	private final ApiUrlBuilder apiUrlBuilder;
	private final ObjectMapper objectMapper;

	public RequestBuilder(ApiUrlBuilder apiUrlBuilder) {
		this.apiUrlBuilder = apiUrlBuilder;
		this.objectMapper = new ObjectMapper();
	}
	
	public HttpPost buildCreateInstanceRequest(String id, String name, String timeZone) throws JsonGenerationException, JsonMappingException, IOException {
		HttpEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(new Instance(id, name, DateTime.now().toDate(), timeZone)), UTF8);
		final HttpPost post = new HttpPost(apiUrlBuilder.getInstancesUrl());
		post.setEntity(entity);
		return post;
	}
	
	public HttpPut buildUpdateInstanceRequest(Instance instance) throws JsonGenerationException, JsonMappingException, IOException {
		final HttpPut put = new HttpPut(apiUrlBuilder.getInstancesUrl() + "/" + instance.getId());
		HttpEntity entity = new StringEntity(objectMapper.writeValueAsString(instance), UTF8);
		put.setEntity(entity);
		return put;
	}
	
	public HttpPost buildCreateMemberRequest(String instance, String firstName, String lastName, List<Squad> squads, String email, String password, Date dateOfBirth, String role) throws JsonGenerationException, JsonMappingException, IOException {
		Member member = new Member(firstName, lastName, squads, email, password, dateOfBirth);	// TODO this should be the argument
		member.setRole(role);
		
		final HttpEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(member), UTF8);
		final HttpPost post = new HttpPost(apiUrlBuilder.getMembersUrl(instance));
		post.setEntity(entity);
		return post;
	}
		
	public HttpPost buildUpdateMemberRequest(String instance, Member member) throws JsonGenerationException, JsonMappingException, IOException {
		final HttpEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(member), UTF8);
		final HttpPost post = new HttpPost(apiUrlBuilder.getMemberDetailsUrl(instance, member.getId()));
		post.setEntity(entity);
		return post;
	}
	
	public HttpPost buildUpdateSquadRequest(String instance, Squad squad) throws JsonGenerationException, JsonMappingException, IOException {
		final HttpEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(squad), UTF8);		
		final HttpPost post = new HttpPost(apiUrlBuilder.getSquadUrl(instance, squad.getId()));
		post.setEntity(entity);
		return post;
	}
	
	public HttpPost buildSetSquadMembersRequest(String instance, String squadId, Set<String> members) throws JsonGenerationException, JsonMappingException, IOException {
		final HttpEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(members), UTF8);
		final HttpPost post = new HttpPost(apiUrlBuilder.getSquadUrl(instance, squadId) + "/members");
		post.setEntity(entity);
		return post;
	}
	
	public HttpPost buildCreateOutingPost(String instance, Outing outing, int repeats) throws JsonGenerationException, JsonMappingException, IOException {
		return buildOutingPostTo(outing, apiUrlBuilder.getOutingsUrl(instance), repeats);
	}

	public HttpPost buildUpdateOutingPost(String instance, Outing outing) throws JsonGenerationException, JsonMappingException, IOException {
		return buildOutingPostTo(outing, apiUrlBuilder.getOutingUrl(instance, outing.getId()), null);
	}
	
	public HttpPost buildResetPasswordRequest(String instance, String username) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getResetPasswordUrl(instance));
		
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("username", username));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
	public HttpDelete buildDeleteInstanceRequest(String id) {
		return new HttpDelete(apiUrlBuilder.getInstanceUrl(id));
	}

	public HttpPost buildConfirmPasswordRequest(String instance, String token) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getConfirmResetPasswordUrl(instance));
		
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("token", token));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
	public HttpPost buildAuthPost(String instance, String username, String password) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getAuthUrlFor(instance));			
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("username", username));
		nameValuePairs.add(new BasicNameValuePair("password", password));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
	public HttpPost buildAuthFacebookPost(String instance, String token) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getAuthUrlFor(instance) + "/facebook");
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("token", token));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
	public HttpPost buildChangePasswordPost(String instance, String memberId, String currentPassword, String newPassword) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getMemberDetailsUrl(instance, memberId) + "/password");
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("currentPassword", currentPassword));
		nameValuePairs.add(new BasicNameValuePair("newPassword", newPassword));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
	private HttpPost buildOutingPostTo(Outing outing, String url, Integer repeats) throws IOException, JsonGenerationException, JsonMappingException {
		final HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(outing), UTF8));
		if (repeats != null) {
			Header header = new BasicHeader("repeats", Integer.toString(repeats));
			post.addHeader(header);
		}
		return post;
	}
	
}
