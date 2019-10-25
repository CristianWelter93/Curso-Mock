package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Carteiro;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EncerradorDeLeilaoTest {

    private Carteiro carteiro;
    private RepositorioDeLeiloes daoFalso;

    @Before
    public void criaDaoFalso() {
        daoFalso = mock(RepositorioDeLeiloes.class);
    }

    @Before
    public void criaEnviadorDeEmailFalso() {
        Carteiro carteiroFalso = mock(Carteiro.class);
        carteiro = carteiroFalso;
    }


    @Test
    public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
        List<Leilao> leiloesantigos = Arrays.asList(leilao1, leilao2);

        when(daoFalso.correntes()).thenReturn(leiloesantigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        assertEquals(2, encerrador.getTotalEncerrados());
        assertTrue(leilao1.isEncerrado());
        assertTrue(leilao2.isEncerrado());
    }

    @Test
    public void naodeveEncerrarLeiloesQueComecaramOntem() {
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();
        List<Leilao> leiloesantigos = Arrays.asList(leilao1, leilao2);

        when(daoFalso.correntes()).thenReturn(leiloesantigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
    }

    @Test
    public void naoExisteLeilao() {
        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -14);

        List<Leilao> leiloesantigos = new ArrayList<Leilao>();

        when(daoFalso.correntes()).thenReturn(leiloesantigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());

    }

    @Test
    public void deveAtualizarLeiloesEncerrados() {
        Calendar dezDiasAtras = Calendar.getInstance();
        dezDiasAtras.add(Calendar.DAY_OF_MONTH, -10);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();

        List<Leilao> leiloesantigos = Arrays.asList(leilao1);

        when(daoFalso.correntes()).thenReturn(leiloesantigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        verify(daoFalso, times(1)).atualiza(leilao1);

    }

    @Test
    public void deveExecutarAtualizaLeilaoEDepoisEnviaLeilao() {

        Calendar dezDiasAtras = Calendar.getInstance();
        dezDiasAtras.add(Calendar.DAY_OF_MONTH, -10);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();

        List<Leilao> leiloesantigos = Arrays.asList(leilao1);
        when(daoFalso.correntes()).thenReturn(leiloesantigos);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        // passamos os mocks que serao verificados
        InOrder inOrder = inOrder(daoFalso, carteiro);
        // a primeira invocação
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
        // a segunda invocação
        inOrder.verify(carteiro, times(1)).envia(leilao1);
    }

    @Test
    public void deveExecutarUmLeilaoMesmoComOutroDandoProblema() {

        Calendar dezDiasAtras = Calendar.getInstance();
        dezDiasAtras.add(Calendar.DAY_OF_MONTH, -10);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();

        List<Leilao> leiloesantigos = Arrays.asList(leilao1, leilao2);
        when(daoFalso.correntes()).thenReturn(leiloesantigos);
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        verify(carteiro, times(0)).envia(leilao1);
        verify(daoFalso).atualiza(leilao2);
        verify(carteiro).envia(leilao2);

    }

    @Test
    public void nuncaDeveInvocarCarteiroSeNenhumDaoFuncionar() {

        Calendar dezDiasAtras = Calendar.getInstance();
        dezDiasAtras.add(Calendar.DAY_OF_MONTH, -10);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("TV de Plasma").naData(dezDiasAtras).constroi();

        List<Leilao> leiloesantigos = Arrays.asList(leilao1, leilao2);
        when(daoFalso.correntes()).thenReturn(leiloesantigos);
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao2);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
        encerrador.encerra();

        verify(carteiro, never()).envia(leilao1);
        verify(carteiro, never()).envia(leilao2);

    }
}
