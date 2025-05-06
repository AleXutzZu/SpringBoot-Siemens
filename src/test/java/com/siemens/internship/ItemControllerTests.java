package com.siemens.internship;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    void fetchAllItems() throws Exception {
        itemRepository.saveAll(List.of(
                new Item(null, "Item 1", "Desc 1", "Done", "test1@example.com"),
                new Item(null, "Item 2", "Desc 2", "Processing", "test2@example.com"),
                new Item(null, "Item 3", "Desc 3", "Waiting", "test3@example.com")
        ));

        MvcResult result = mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        List<Item> items = objectMapper.readValue(content, new TypeReference<>() {
        });

        assertThat(items).hasSize(3);
        assertThat(items.get(0).getName()).isEqualTo("Item 1");
        assertThat(items.get(1).getName()).isEqualTo("Item 2");
        assertThat(items.get(2).getName()).isEqualTo("Item 3");
    }

    @Test
    void createItemSuccessfully() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Waiting", "test1@example.com");

        MvcResult result = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Item responseItem = objectMapper.readValue(responseContent, Item.class);

        assertThat(responseItem.getName()).isEqualTo("Item 1");
        assertThat(responseItem.getDescription()).isEqualTo("Desc 1");
    }

    @Test
    void createItemInvalidEmail() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Done", "invalid");
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchItemSuccessfully() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Trying", "test1@example.com");

        item = itemRepository.save(item);

        MvcResult result = mockMvc.perform(get("/api/items/" + item.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Item responseItem = objectMapper.readValue(content, Item.class);
        assertThat(responseItem.getName()).isEqualTo(item.getName());
        assertThat(responseItem.getDescription()).isEqualTo(item.getDescription());
        assertThat(responseItem.getEmail()).isEqualTo(item.getEmail());
        assertThat(responseItem.getId()).isEqualTo(item.getId());
    }

    @Test
    void fetchItemNotFound() throws Exception {
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemSuccessfully() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Done", "test1@example.com");

        item = itemRepository.save(item);

        item.setStatus("Working");

        MvcResult result = mockMvc.perform(put("/api/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk()).andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Item responseItem = objectMapper.readValue(responseContent, Item.class);
        assertThat(responseItem.getName()).isEqualTo(item.getName());
        assertThat(responseItem.getDescription()).isEqualTo(item.getDescription());
        assertThat(responseItem.getEmail()).isEqualTo(item.getEmail());
        assertThat(responseItem.getId()).isEqualTo(item.getId());
        assertThat(responseItem.getStatus()).isEqualTo(item.getStatus());
    }

    @Test
    void updateItemInvalidEmail() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Done", "test1@example.com");
        item = itemRepository.save(item);
        item.setEmail("Invalid");

        mockMvc.perform(put("/api/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemNotFound() throws Exception {
        Item item = new Item(10L, "Item 1", "Desc 1", "Done", "test1@example.com");

        mockMvc.perform(put("/api/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemNullFields() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Done", "test1@example.com");

        item = itemRepository.save(item);

        item.setEmail(null);
        item.setName(null);

        MvcResult result = mockMvc.perform(put("/api/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk()).andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Item responseItem = objectMapper.readValue(responseContent, Item.class);

        assertThat(responseItem.getName()).isEqualTo(item.getName());
        assertThat(responseItem.getDescription()).isEqualTo(item.getDescription());
        assertThat(responseItem.getEmail()).isEqualTo(item.getEmail());
        assertThat(responseItem.getId()).isEqualTo(item.getId());
        assertThat(responseItem.getStatus()).isEqualTo(item.getStatus());
    }

    @Test
    void deleteItemSuccessfully() throws Exception {
        Item item = new Item(null, "Item 1", "Desc 1", "Done", "test1@example.com");
        item = itemRepository.save(item);

        mockMvc.perform(delete("/api/items/" + item.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItemNotFound() throws Exception {
        mockMvc.perform(delete("/api/items/999"))
                .andExpect(status().isNotFound());
    }
}
