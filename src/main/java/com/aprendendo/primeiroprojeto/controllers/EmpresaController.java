package com.aprendendo.primeiroprojeto.controllers;

import java.text.ParseException;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aprendendo.primeiroprojeto.dtos.EmpresaDto;
import com.aprendendo.primeiroprojeto.entities.Empresa;
import com.aprendendo.primeiroprojeto.response.Response;
import com.aprendendo.primeiroprojeto.services.EmpresaService;

import org.springframework.validation.*;

@RestController
@RequestMapping(path = "/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {
	
	@Autowired
	private EmpresaService empresaService;
	
	public EmpresaController() {
		
	}
	
	@RequestMapping(path="/cnpj/{cnpj}", method=RequestMethod.GET)
	public ResponseEntity<Response<EmpresaDto>> buscarEmpresaPorCnpj(@PathVariable("cnpj") String cnpj){
		Response<EmpresaDto> response = new Response<EmpresaDto>();
		Optional<Empresa> empresa = empresaService.buscarPorCnpj(cnpj);
		
		if(!empresa.isPresent()) {
			response.getErrors().add("Empresa não encontrada.");
			return ResponseEntity.badRequest().body(response);
		}
		
		
		response.setData(this.converterEmpresaDto(empresa.get()));
		return ResponseEntity.ok(response);		
	}

	private EmpresaDto converterEmpresaDto(Empresa empresa) {
		EmpresaDto empresaDto = new EmpresaDto();
		
		empresaDto.setId(empresa.getId());
		empresaDto.setCnpj(empresa.getCnpj());
		empresaDto.setRazaoSocial(empresa.getRazaoSocial());
		
		return empresaDto;
	}
	
	@RequestMapping(path="/cnpj/{cnpj}", method=RequestMethod.PUT)
	public ResponseEntity<Response<EmpresaDto>> alterarEmpresa(@PathVariable("cnpj") String cnpj, 
			@Valid @RequestBody EmpresaDto empresaDto, BindingResult result){
		Response<EmpresaDto> response = new Response<EmpresaDto>();
		validarEmpresa(empresaDto, result);
		
		empresaDto.setCnpj(cnpj);
		
		Empresa empresa = this.converterDtoParaEmpresa(empresaDto, result);
		
		if (result.hasErrors()) {
			
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		empresa = this.empresaService.persistir(empresa);
		response.setData(this.converterEmpresaDto(empresa));
		return ResponseEntity.ok(response);
	}
	
	
	private Empresa converterDtoParaEmpresa(EmpresaDto empresaDto, BindingResult result) {
		Empresa empresa = new Empresa();
		
		if(empresaDto.getCnpj() != null) {
			Optional<Empresa> emp = this.empresaService.buscarPorCnpj(empresaDto.getCnpj());
			if (emp.isPresent()) {
				empresa = emp.get();
			} else {
				result.addError(new ObjectError("Empresa", "Empresa não encontrada."));
			}
		} else {
			result.addError(new ObjectError("Empresa", "Empresa não encontrada2."));
		}
		empresa.setId(empresaDto.getId());
		empresa.setCnpj(empresaDto.getCnpj());
		empresa.setRazaoSocial(empresaDto.getRazaoSocial());
		
		return empresa;
		
	}
	
	private void validarEmpresa(EmpresaDto empresaDto, BindingResult result) {
		if(empresaDto.getCnpj() == null) {
			result.addError(new org.springframework.validation.ObjectError("Empresa","Empresa não informada."));
			return;
		}
		
		Optional<Empresa> empresa = this.empresaService.buscarPorCnpj(empresaDto.getCnpj());
		if (!empresa.isPresent()) {
			result.addError(new org.springframework.validation.ObjectError("Empresa","Empresa não encontrada. CNPJ inexistente"));
		}
	}
	
}
