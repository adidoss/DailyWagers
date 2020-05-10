package com.ContibutorService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ContibutorService.models.Beneficiary;
import com.ContibutorService.models.Donor;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

@CrossOrigin("*")
@RestController
public class ContibutorContoller {
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private final String spreadsheetId = "1sR8mzuk0f9uSf8GL4AU7niYiDSayDsvF4yVSR98AYF8";
    
    @Autowired
    private Environment env;
    
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private MailBuilder mailBuilder;
    
	@PostMapping("/addDonor")
    public void addDonor(@RequestBody Donor donor) throws GeneralSecurityException, IOException {
//    	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//    	String range = "Copy of Donor!C3:F143";
//    	Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, GoogleAuthorizeUtil.getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    	
//    	List<List<Object>> values = Arrays.asList(
//    	        Arrays.asList(
//    	                donor.getName(),donor.getEmail(),donor.getContact(),donor.getCity()
//    	        )
//    	);
//    	
//    	ValueRange body = new ValueRange().setValues(values);
//		AppendValuesResponse result =
//    	        service.spreadsheets().values().append(spreadsheetId, range, body)
//    	                .setValueInputOption("RAW")
//    	                .execute();
    	System.out.println(donor.getName());
    	Beneficiary beneficiary = getBeneficiary();
    	System.out.println(env.getProperty("spring.mail.username"));
    	
    	sendMail(env.getProperty("spring.mail.username"), donor.getEmail(), beneficiary);
    	
    }
    
    public Beneficiary getBeneficiary() throws GeneralSecurityException, IOException {
    	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    	Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, GoogleAuthorizeUtil.getCredentials(HTTP_TRANSPORT))
    								.setApplicationName(APPLICATION_NAME)
    								.build();
    	
    	String range = "BOM - Beneficiary Database!A2:Q";
    	//String updateRange = "BOM - Beneficiary Database!N";
    	ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    	List<List<Object>> values = response.getValues();
    	System.out.println(values.get(0).size());
    	Beneficiary beneficiary = new Beneficiary();
    	
    	for(int i=0;i<values.size();i++) {
    		if(values.get(i).get(14).equals("TRUE")) {
    			beneficiary.setName(values.get(i).get(3).toString());
    			beneficiary.setContact(values.get(i).get(4).toString());
    			beneficiary.setAddress(values.get(i).get(5).toString());
//    			updateRange+=""+(i+2);
//    			System.out.println(updateRange);
//    			values = Arrays.asList(
//    	    	        Arrays.asList(
//    	    	                "FALSE"
//    	    	        )
//    	    	);
//    			ValueRange body = new ValueRange().setValues(values);
//    			Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, updateRange, body);
//    			request.setValueInputOption("USER_ENTERED");
//    			request.execute();
    			break;
    		}
    	}
    	return beneficiary;
		
	}


	public void sendMail(String from, String to, Beneficiary beneficiary) {
    	MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject("Thank You");
            String content = mailBuilder.build(beneficiary);
            messageHelper.setText(content, true);
            
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
}