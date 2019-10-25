package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RelogioDosistema;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GeradorDePagamentoTest {

    @Test
    public void deveGerarPagamentoParaUmLeilaoEncerrado() {
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        Avaliador avaliador = new Avaliador();

        Leilao leilao = new CriadorDeLeilao().para("play")
                .lance(new Usuario("Joao"), 500.0)
                .lance(new Usuario("Paulo"), 800.0)
                .constroi();
        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
        avaliador.avalia(leilao);

        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());

        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(800, pagamentoGerado.getValor(), 0.00001);

    }

    @Test
    public void deveEmpurrarParaOProximoDiaUtilseForSabado() {
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        RelogioDosistema relogio = mock(RelogioDosistema.class);

        Leilao leilao = new CriadorDeLeilao().para("play")
                .lance(new Usuario("Joao"), 500.0)
                .lance(new Usuario("Paulo"), 800.0)
                .constroi();
        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        Calendar sabado = Calendar.getInstance();
        sabado.set(2012, Calendar.APRIL, 7);
        when(relogio.hoje()).thenReturn(sabado);


        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());

        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void deveEmpurrarParaOProximoDiaUtilseForDomingo() {
        RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
        RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
        RelogioDosistema relogio = mock(RelogioDosistema.class);

        Leilao leilao = new CriadorDeLeilao().para("play")
                .lance(new Usuario("Joao"), 500.0)
                .lance(new Usuario("Paulo"), 800.0)
                .constroi();
        when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

        Calendar domingo = Calendar.getInstance();
        domingo.set(2012, Calendar.APRIL, 8);
        when(relogio.hoje()).thenReturn(domingo);

        GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
        gerador.gera();

        ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).salva(argumento.capture());

        Pagamento pagamentoGerado = argumento.getValue();

        assertEquals(9, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
    }
}
