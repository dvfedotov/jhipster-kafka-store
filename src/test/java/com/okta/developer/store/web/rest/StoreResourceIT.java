package com.okta.developer.store.web.rest;

import com.okta.developer.store.StoreApp;
import com.okta.developer.store.config.TestSecurityConfiguration;
import com.okta.developer.store.domain.Store;
import com.okta.developer.store.repository.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.okta.developer.store.domain.enumeration.StoreStatus;
/**
 * Integration tests for the {@link StoreResource} REST controller.
 */
@SpringBootTest(classes = { StoreApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class StoreResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final StoreStatus DEFAULT_STATUS = StoreStatus.OPEN;
    private static final StoreStatus UPDATED_STATUS = StoreStatus.CLOSED;

    private static final Instant DEFAULT_CREATE_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATE_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATE_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATE_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MockMvc restStoreMockMvc;

    private Store store;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Store createEntity() {
        Store store = new Store()
            .name(DEFAULT_NAME)
            .address(DEFAULT_ADDRESS)
            .status(DEFAULT_STATUS)
            .createTimestamp(DEFAULT_CREATE_TIMESTAMP)
            .updateTimestamp(DEFAULT_UPDATE_TIMESTAMP);
        return store;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Store createUpdatedEntity() {
        Store store = new Store()
            .name(UPDATED_NAME)
            .address(UPDATED_ADDRESS)
            .status(UPDATED_STATUS)
            .createTimestamp(UPDATED_CREATE_TIMESTAMP)
            .updateTimestamp(UPDATED_UPDATE_TIMESTAMP);
        return store;
    }

    @BeforeEach
    public void initTest() {
        storeRepository.deleteAll();
        store = createEntity();
    }

    @Test
    public void createStore() throws Exception {
        int databaseSizeBeforeCreate = storeRepository.findAll().size();
        // Create the Store
        restStoreMockMvc.perform(post("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isCreated());

        // Validate the Store in the database
        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeCreate + 1);
        Store testStore = storeList.get(storeList.size() - 1);
        assertThat(testStore.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testStore.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testStore.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testStore.getCreateTimestamp()).isEqualTo(DEFAULT_CREATE_TIMESTAMP);
        assertThat(testStore.getUpdateTimestamp()).isEqualTo(DEFAULT_UPDATE_TIMESTAMP);
    }

    @Test
    public void createStoreWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = storeRepository.findAll().size();

        // Create the Store with an existing ID
        store.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restStoreMockMvc.perform(post("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isBadRequest());

        // Validate the Store in the database
        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = storeRepository.findAll().size();
        // set the field null
        store.setName(null);

        // Create the Store, which fails.


        restStoreMockMvc.perform(post("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isBadRequest());

        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkAddressIsRequired() throws Exception {
        int databaseSizeBeforeTest = storeRepository.findAll().size();
        // set the field null
        store.setAddress(null);

        // Create the Store, which fails.


        restStoreMockMvc.perform(post("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isBadRequest());

        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkCreateTimestampIsRequired() throws Exception {
        int databaseSizeBeforeTest = storeRepository.findAll().size();
        // set the field null
        store.setCreateTimestamp(null);

        // Create the Store, which fails.


        restStoreMockMvc.perform(post("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isBadRequest());

        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllStores() throws Exception {
        // Initialize the database
        storeRepository.save(store);

        // Get all the storeList
        restStoreMockMvc.perform(get("/api/stores?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(store.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].createTimestamp").value(hasItem(DEFAULT_CREATE_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].updateTimestamp").value(hasItem(DEFAULT_UPDATE_TIMESTAMP.toString())));
    }
    
    @Test
    public void getStore() throws Exception {
        // Initialize the database
        storeRepository.save(store);

        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", store.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(store.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.createTimestamp").value(DEFAULT_CREATE_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.updateTimestamp").value(DEFAULT_UPDATE_TIMESTAMP.toString()));
    }
    @Test
    public void getNonExistingStore() throws Exception {
        // Get the store
        restStoreMockMvc.perform(get("/api/stores/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateStore() throws Exception {
        // Initialize the database
        storeRepository.save(store);

        int databaseSizeBeforeUpdate = storeRepository.findAll().size();

        // Update the store
        Store updatedStore = storeRepository.findById(store.getId()).get();
        updatedStore
            .name(UPDATED_NAME)
            .address(UPDATED_ADDRESS)
            .status(UPDATED_STATUS)
            .createTimestamp(UPDATED_CREATE_TIMESTAMP)
            .updateTimestamp(UPDATED_UPDATE_TIMESTAMP);

        restStoreMockMvc.perform(put("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedStore)))
            .andExpect(status().isOk());

        // Validate the Store in the database
        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate);
        Store testStore = storeList.get(storeList.size() - 1);
        assertThat(testStore.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testStore.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testStore.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testStore.getCreateTimestamp()).isEqualTo(UPDATED_CREATE_TIMESTAMP);
        assertThat(testStore.getUpdateTimestamp()).isEqualTo(UPDATED_UPDATE_TIMESTAMP);
    }

    @Test
    public void updateNonExistingStore() throws Exception {
        int databaseSizeBeforeUpdate = storeRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStoreMockMvc.perform(put("/api/stores").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(store)))
            .andExpect(status().isBadRequest());

        // Validate the Store in the database
        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteStore() throws Exception {
        // Initialize the database
        storeRepository.save(store);

        int databaseSizeBeforeDelete = storeRepository.findAll().size();

        // Delete the store
        restStoreMockMvc.perform(delete("/api/stores/{id}", store.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Store> storeList = storeRepository.findAll();
        assertThat(storeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
