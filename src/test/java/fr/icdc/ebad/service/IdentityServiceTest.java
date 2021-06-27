package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.security.KeyPair;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    @InjectMocks
    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;


    @Test
    public void saveIdentity() {
        Identity identity = Identity.builder().name("testName").login("testLogin").build();
        Identity identitySaved = Identity.builder().id(1L).name("testName").login("testLogin").build();

        when(identityRepository.save(eq(identity))).thenReturn(identitySaved);
        Identity result = identityService.saveIdentity(identity);
        assertEquals(identitySaved, result);
        verify(identityRepository, times(1)).save(eq(identity));
    }

    @Test
    public void getIdentity() {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").build();

        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));
        Optional<Identity> result = identityService.getIdentity(1L);
        assertEquals(identity, result.get());
        verify(identityRepository, times(1)).findById(eq(1L));
    }

    @Test
    public void deleteIdentity() {
        identityService.deleteIdentity(1L);
        verify(identityRepository, times(1)).deleteById(eq(1L));
    }

    @Test
    public void findWithoutApp() {
        Pageable pageable = Pageable.unpaged();
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").build();

        Page<Identity> identities = new PageImpl<>(Collections.singletonList(identity));
        when(identityRepository.findAllByAvailableApplicationNull(eq(pageable))).thenReturn(identities);
        Page<Identity> result = identityService.findWithoutApp(pageable);
        assertEquals(identity, result.getContent().get(0));
    }

    @Test
    public void findAllByApplication() {
        Pageable pageable = Pageable.unpaged();
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").build();

        Page<Identity> identities = new PageImpl<>(Collections.singletonList(identity));
        when(identityRepository.findAllByAvailableApplicationId(eq(1L), eq(pageable))).thenReturn(identities);
        Page<Identity> result = identityService.findAllByApplication(1L, pageable);
        assertEquals(identity, result.getContent().get(0));
    }

    @Test
    public void createKeyPair() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").privatekeyPath("src/test/resources/fakekeys/fake_key_password").passphrase("password").build();

        KeyPair result = identityService.createKeyPair(identity);
        assertNotNull(result);
    }

    @Test
    public void createKeyPair2() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").privatekeyPath("src/test/resources/fakekeys/fake_key_no_password").build();

        KeyPair result = identityService.createKeyPair(identity);
        assertNotNull(result);
    }

    @Test(expected = EbadServiceException.class)
    public void createKeyPair3() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").privatekeyPath("src/test/resources/fakekeys/fake_key_bad_format").build();
        identityService.createKeyPair(identity);
    }

    @Test(expected = EbadServiceException.class)
    public void createKeyPair4() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").build();
        identityService.createKeyPair(identity);
    }

    @Test(expected = EbadServiceException.class)
    public void createKeyPair5() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").privatekeyPath("src/test/resources/fakekeys/fake_key_password").build();
        identityService.createKeyPair(identity);
    }

    @Test
    public void createKeyPair6() throws EbadServiceException {
        Identity identity = Identity.builder().id(1L).name("testName").login("testLogin").privatekey("-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIG4wIBAAKCAYEAx15eCto5yBKKq8prOMwhGqFDe8OM7y0Qc16vhXc5ax8/WVVW\n" +
                "S6Ei3BZzQ8Te6Qu3fDp3Z2nro/p2Qb4RAY2ZBadorqYzpGs4MD6rIv1l2Wz/e++S\n" +
                "5PHd9/KShGa/hSoHpwL5TgIaNT0PtRY4XRT4f2/ZFwTT92he4mNHCzRld/Kwi5rk\n" +
                "QgLQe18ZCECqlBXDse6fvWlJaQZSszGmUOfp+O4cZ00zIIw0Ax9/3wDuUsHkjLnl\n" +
                "JOls3MSYY8w+MNquDh5Xslrr9Idve7HFIoS4CTzbM7zXlbIQiYZIWEBBEkhik7HB\n" +
                "FZIuU0sTj/l+5HGF03NLRf7lzkynmfSasyr7u9lXycL+jVek2SBFouGWmuBP2NBv\n" +
                "QulhXvyxudMetruf2SH3vARbafgZXzEsElDXoFA7JP0JxbjYlpffkCD5MDBDC5VU\n" +
                "AQnDDnAq078f4rdf6W8StnL8JiwiHa1LGehhe7fmn2tIyp8gP9d+GuqDBmnAqyjl\n" +
                "cvQMcMRxG9NCq1qLAgMBAAECggGAH7E+ndcG7GkWJiztUoAQmx0bjycM1lCBlvVs\n" +
                "TXkgZYj4FwlbyHX7BqWC+Tjofn9gUZ0xsfzCerSr69N8/JQ3yRBNscW12+M3cWPk\n" +
                "7JD/icqr6lWxMjfIo0uqQuXaeB6wTLpKSz5GUEQ+pPi9SP62afurtN8Nmvy6aJ6D\n" +
                "gqJ3ptOrgOXEd0HfRl1dq1QsSZIUI8rdFc0Q4GMi7l1sJNxY8ztWxI0fHYNsGTr+\n" +
                "psKNIBa2cDB03qNErOuLlgG3uShpzsPnEc0iz1QXWCfbTCgf93a7zs/DbdrFCd2d\n" +
                "2fYjQ6iA8CdT5SYePLvm15GRYgEwxvcda53/DUBvWV6QQ6S1Awojd7I/DoASD+UW\n" +
                "ep9Jedb/3M/xP/tKZ07w6oMqEk5qNfOOYxaUHa+MSKSz8wsLZjyfGHvAy951GN+t\n" +
                "bChwlqh7B0QxG+dt5boQgkjzNMgxqtbVr3q4J5IGBfy87UuUBc8AOvXZ1npQBAJw\n" +
                "WLLTk6hkJwestRpFB8wcPOM0HwSRAoHBAPR6Ls16D8/Nhfem6TlPdIqTP9440XMU\n" +
                "dOJBzfe1SM945zB9Fv2G1v7AkGRZ8RYK9lK0YLVE1rpkXFd0JsTg8x8edoZJuvRi\n" +
                "YZQD5P5eYyMc9G+VUuxznlant47yDwfnuaoLNSfOjMaHsv1T9GqHC99Owr8EpWJ1\n" +
                "Nu37R6AbZlDrKE51qcXtgjt/T/kXaKUjkxB/wpc31BUV4Z9i6n4iGHBqgoBL3ScY\n" +
                "0JGiFFbTmMBoYo7AZSA535GnCk2oFBqsDQKBwQDQw+l0R84gIi7OH+gl3jk7wgzS\n" +
                "miI08S7URNQ50PjnpqCTHcLmmnfxKj8RRWW2v1j/Gh8I85eS6Zss7c+hskFuTuT0\n" +
                "HEMJ3gDG08e1U3poEu5pFpgKEwTGWGlyYllqHC4GpdCeaKS17RVWGBRTbgeuIh6U\n" +
                "+1vL6eq0oiPC3DlNO1UII6L0eYliQCuXiQ+1mxyieBKVdzi4U00GdF/e2JWUPwoj\n" +
                "ANUMWUgaqXyPkrTXFZI4OpsKEN1zT25ZaYXkQvcCgcEA1pjVsmn9rSOz/8IUoMjc\n" +
                "/LLqci8fs0t2mhC3MkOACRYQoaB76MlOuUngtSW6GOZAeDJ7XUJy1iQ4Zk+/pDvt\n" +
                "TRiZY6EeqVweWSXeAWZq0SyeZ8AEjSekCl6oIqLZjM/cSqYR38JQEgiVlgb22fd2\n" +
                "9WqWScRTRAytzdIvehvzAP6aX9yqzOGB2qYTVgw1QBftBKOQ9Nn9oRCW/yh9u4BE\n" +
                "QZFMwj9VffcrRVqnTdvpDqPSN4oselAyrhCZW7c5BnyVAoHAbiWPvSxNkyK180de\n" +
                "Yux24hKmVU/BkJFYFeMi3kZlrUN7IWPoe2cwtWuGzwQAgIVA3YfYrA5qALeEihYH\n" +
                "INc+MocpwapPrJsXadA/ZGphARxL8eJb4aTbNhNbv2AosRWhKxy9j3bCwMIQKdaW\n" +
                "ktZQ8w1JeW2Np28JyLhridpL2XXejWbQCZE+bTpSRaepRDZMy2Py2i2HHanF0AwC\n" +
                "sT8w3IDORl2gCt3obzjRYacUBw94kHZLauovDTHxlPdaD29NAoHAQ6vYDI3SquBi\n" +
                "MYsAMIXsqAZmde21MauC39Mlwvitr8KYmUtvg8gTq2B3rHPlQrrc0P+eiSRyO5dM\n" +
                "2Fq2xHizN0Zvtk5Azsz46WVvKajgNfK4eC41/GYToarUdCZw1pZ17ZlT8dQZ4UJi\n" +
                "paQf+WFVuOo95UVMjXvz46Oc6ooY2Wj7pubMzRgxAP6u9WxcTdsQP8IfoV3TSWuS\n" +
                "E6hgsg8DhlNjHtjI8U+bAGp+RSXMH9hNTsjjeI31mSSKQ32IoEJr\n" +
                "-----END RSA PRIVATE KEY-----").build();

        KeyPair result = identityService.createKeyPair(identity);
        assertNotNull(result);
    }
}