package com.worthybitbuilders.squadsense.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.worthybitbuilders.squadsense.R;
import com.worthybitbuilders.squadsense.adapters.EditBoardsAdapter;
import com.worthybitbuilders.squadsense.adapters.StatusContentsAdapter;
import com.worthybitbuilders.squadsense.adapters.StatusEditItemAdapter;
import com.worthybitbuilders.squadsense.adapters.TableViewAdapter;
import com.worthybitbuilders.squadsense.databinding.ActivityBoardBinding;
import com.worthybitbuilders.squadsense.databinding.BoardAddItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardAddNewRowPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardDateItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardEditBoardsViewBinding;
import com.worthybitbuilders.squadsense.databinding.BoardNumberItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardStatusEditNewItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardStatusEditViewBinding;
import com.worthybitbuilders.squadsense.databinding.BoardStatusItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardTextItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.BoardTimelineItemPopupBinding;
import com.worthybitbuilders.squadsense.databinding.CustomLoadingDialogBinding;
import com.worthybitbuilders.squadsense.models.board_models.BoardCheckboxItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardColumnHeaderModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardContentModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardDateItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardNumberItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardStatusItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardTextItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardTimelineItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardUpdateItemModel;
import com.worthybitbuilders.squadsense.models.board_models.BoardUserItemModel;
import com.worthybitbuilders.squadsense.utils.CustomUtils;
import com.worthybitbuilders.squadsense.utils.DialogUtils;
import com.worthybitbuilders.squadsense.viewmodels.ProjectActivityViewModel;
import com.worthybitbuilders.squadsense.viewmodels.BoardViewModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectActivity extends AppCompatActivity {
    private TableViewAdapter boardAdapter;
    private ProjectActivityViewModel projectActivityViewModel;

    // This differs from "projectActivityViewModel", this holds logic for only TableView
    private BoardViewModel boardViewModel;
    private ActivityBoardBinding activityBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        activityBinding = ActivityBoardBinding.inflate(getLayoutInflater());
        activityBinding.btnShowTables.setOnClickListener(view -> showTables());

        projectActivityViewModel = new ViewModelProvider(this).get(ProjectActivityViewModel.class);
        boardViewModel = new ViewModelProvider(this).get(BoardViewModel.class);

        boardAdapter = new TableViewAdapter(this, boardViewModel, new TableViewAdapter.OnClickHandlers() {
            @Override
            public void OnTimelineItemClick(BoardTimelineItemModel itemModel, String columnTitle, int columnPos, int rowPos) {
                showTimelineItemPopup(itemModel, columnTitle, columnPos, rowPos);
            }

            @Override
            public void OnDateItemClick(BoardDateItemModel itemModel, String columnTitle, int columnPos, int rowPos) {
                showDateItemPopup(itemModel, columnTitle, columnPos, rowPos);
            }

            @Override
            public void onCheckboxItemClick(BoardCheckboxItemModel itemModel, int columnPos, int rowPos) {
                Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
                loadingDialog.show();
                itemModel.setChecked(!itemModel.getChecked());
                boardViewModel.updateACell(itemModel).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
                        } else {
                            Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                        }

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();
                    }
                });

            }

            @Override
            public void onUpdateItemClick(BoardUpdateItemModel itemModel, String columnTitle) {
                Toast.makeText(ProjectActivity.this, "Update item clicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNumberItemClick(BoardNumberItemModel itemModel, String columnTitle, int columnPos, int rowPos) {
                showNumberItemPopup(itemModel, columnTitle, columnPos, rowPos);
            }

            @Override
            public void onNewColumnHeaderClick() {
                showAddBoardItemPopup();
            }

            @Override
            public void onNewRowHeaderClick() {
                showNewRowPopup();
            }

            @Override
            public void onTextItemClick(BoardTextItemModel itemModel, String columnTitle, int columnPos, int rowPos) {
                showTextItemPopup(itemModel, columnTitle, columnPos, rowPos);
            }

            @Override
            public void onUserItemClick(BoardUserItemModel userItemModel) {
                Toast.makeText(ProjectActivity.this, "User click item handler", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusItemClick(BoardStatusItemModel itemModel, int columnPos, int rowPos) {
                showTaskStatusPopup(itemModel, columnPos, rowPos);
            }
        });
        activityBinding.tableView.setAdapter(boardAdapter);

        projectActivityViewModel.getProjectModelLiveData().observe(this, projectModel -> {
            if (projectModel == null) return;
            // set cells content, pass the adapter to let them call the set item
            BoardContentModel content = projectModel.getBoards().get(projectModel.getChosenPosition());
            boardViewModel.setBoardContent(content, projectModel.get_id(), boardAdapter);
            // set board title for "more table" drop down
            activityBinding.btnShowTables.setText(
                    projectModel.getBoards()
                            .get(projectModel.getChosenPosition())
                            .getBoardTitle());
        });

        getDataForActivity();
        activityBinding.btnBack.setOnClickListener((view) -> onBackPressed());
        setContentView(activityBinding.getRoot());
    }

    /**
     * TODO: better way to handle this ("fetch", "createNew")
     * @whatToDo is the thing that specify how the activity should handle
     * the case
     * One is create new board, it needs to send and get data from server ("createNew")
     * Two is fetch the board which is created before ("fetch")
     */
    private void getDataForActivity() {
        Intent intent = getIntent();
        String whatToDo = intent.getStringExtra("whatToDo");
        Dialog loadingDialog = DialogUtils.GetLoadingDialog(this);
        loadingDialog.show();
        if (whatToDo.equals("createNew")) {
            projectActivityViewModel.saveNewProjectToRemote(new ProjectActivityViewModel.OnGettingProjectFromRemote() {
                @Override
                public void onSuccess() {
                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ProjectActivity.this, "Failed to create new project, please try again", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    finish();
                }
            });
        } else {
            String projectId = intent.getStringExtra("projectId");
            projectActivityViewModel.getProjectById(projectId, new ProjectActivityViewModel.OnGettingProjectFromRemote() {
                @Override
                public void onSuccess() {
                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure(String message) {
                    loadingDialog.dismiss();
                    Toast.makeText(ProjectActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }

    private void showTables() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardEditBoardsViewBinding binding = BoardEditBoardsViewBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        EditBoardsAdapter editBoardsAdapter = new EditBoardsAdapter(this.projectActivityViewModel.getProjectModel(), this);
        editBoardsAdapter.setHandlers(new EditBoardsAdapter.ClickHandlers() {
            @Override
            public void onRemoveClick(int position) {
                projectActivityViewModel.getProjectModel().removeBoardAt(position);
                editBoardsAdapter.notifyItemRemoved(position);
                editBoardsAdapter.notifyItemRangeChanged(position, projectActivityViewModel.getProjectModel().getBoards().size());
            }

            @Override
            public void onRenameClick(int position, String newTitle) {
                projectActivityViewModel.getProjectModel().getBoards().get(position).setBoardTitle(newTitle);
                editBoardsAdapter.notifyItemChanged(position);
                if (position == projectActivityViewModel.getProjectModel().getChosenPosition()) {
                    boardViewModel.setBoardTitle(newTitle);
                    activityBinding.btnShowTables.setText(newTitle);
                }
            }

            @Override
            public void onItemClick(int position) {
                if (position == projectActivityViewModel.getProjectModel().getChosenPosition()) {
                    dialog.dismiss();
                    return;
                }
                projectActivityViewModel.getProjectModel().setChosenPosition(position);
                BoardContentModel newContent = projectActivityViewModel.getProjectModel().getBoards().get(position);
                boardViewModel.setBoardContent(newContent, projectActivityViewModel.getProjectModel().get_id(), boardAdapter);
                activityBinding.btnShowTables.setText(newContent.getBoardTitle());
                dialog.dismiss();
            }
        });
        binding.rvBoards.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBoards.setAdapter(editBoardsAdapter);

        binding.btnClose.setOnClickListener(view -> dialog.dismiss());
        binding.btnNewBoard.setOnClickListener(view -> {
            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();
            projectActivityViewModel.addNewBoardToProject(new ProjectActivityViewModel.OnCreateAndSaveNewBoardToRemote() {
                @Override
                public void onSuccess() {
                    editBoardsAdapter.notifyItemInserted(projectActivityViewModel.getProjectModel().getBoards().size() - 1);
                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ProjectActivity.this, "Unable to add new board, please try again", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                }
            });
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showNewRowPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardAddNewRowPopupBinding binding = BoardAddNewRowPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.btnClosePopup.setOnClickListener(view -> dialog.dismiss());
        binding.btnAdd.setOnClickListener(view -> {
            String newRowTitle = binding.etContent.getText().toString();
            if (newRowTitle.isEmpty()) { dialog.dismiss(); return; }
            boardViewModel.createNewRow(newRowTitle);
            dialog.dismiss();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    private void showTaskStatusPopup(BoardStatusItemModel statusItemModel, int columnPos, int rowPos)
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardStatusItemPopupBinding binding = BoardStatusItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        StatusContentsAdapter statusContentsAdapter = new StatusContentsAdapter(statusItemModel);
        statusContentsAdapter.setHandlers((itemModel, newContent) -> {
            itemModel.setContent(newContent);
            boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
            dialog.dismiss();
        });
        binding.rvStatusContents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStatusContents.setAdapter(statusContentsAdapter);

        binding.btnClose.setOnClickListener(view -> dialog.dismiss());
        binding.btnEditLabels.setOnClickListener(view -> {
            showStatusContentsEdit(statusItemModel, statusContentsAdapter, columnPos, rowPos);
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showStatusContentsEdit(BoardStatusItemModel statusItemModel, StatusContentsAdapter statusContentsAdapter, int columnPos, int rowPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardStatusEditViewBinding binding = BoardStatusEditViewBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        BoardStatusItemModel clonedItemModel = new BoardStatusItemModel(statusItemModel);

        StatusEditItemAdapter statusEditItemAdapter = new StatusEditItemAdapter(clonedItemModel);
        statusEditItemAdapter.setHandlers(new StatusEditItemAdapter.ClickHandlers() {
            @Override
            public void onChooseColorClick(int position, BoardStatusItemModel itemModel) {
                new ColorPickerDialog.Builder(ProjectActivity.this)
                        .setTitle("Choose color")
                        .setPositiveButton("SELECT", (ColorEnvelopeListener) (envelope, fromUser) -> {
                            itemModel.setColorAt(position, '#' + envelope.getHexCode());
                            statusEditItemAdapter.notifyItemChanged(position);
                        })
                        .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                        .attachAlphaSlideBar(true)
                        .attachBrightnessSlideBar(true)
                        .setBottomSpace(12)
                        .show();
            }

            @Override
            public void onDeleteClick(int position, BoardStatusItemModel itemModel) {
                if (position >= itemModel.getContents().size()) return;
                if (Objects.equals(itemModel.getContent(), itemModel.getContents().get(position))) itemModel.setContent("");
                itemModel.removeContentAt(position);
                statusEditItemAdapter.notifyItemRemoved(position);
                statusEditItemAdapter.notifyItemRangeChanged(position, itemModel.getContents().size());
            }
        });

        binding.rvStatusItems.setLayoutManager(new LinearLayoutManager(ProjectActivity.this, LinearLayoutManager.VERTICAL, false));
        binding.rvStatusItems.setAdapter(statusEditItemAdapter);
        binding.btnAdd.setOnClickListener(view -> {
            showAddNewStatusDialog(clonedItemModel, statusEditItemAdapter);
        });

        binding.btnClose.setOnClickListener(view -> dialog.dismiss());
        binding.btnSave.setOnClickListener(view -> {
            statusItemModel.copyDataFromAnotherInstance(clonedItemModel);

            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();
            boardViewModel.updateACell(statusItemModel).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        boardAdapter.changeCellItem(columnPos, rowPos, statusItemModel);
                        statusContentsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ProjectActivity.this, "Unable to save", Toast.LENGTH_LONG).show();
                    }

                    loadingDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProjectActivity.this, "Unable to save", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }
            });
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showAddNewStatusDialog(BoardStatusItemModel itemModel, StatusEditItemAdapter statusEditItemAdapter) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardStatusEditNewItemPopupBinding binding = BoardStatusEditNewItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());
        binding.btnAdd.setOnClickListener(view -> {
            String newContent = binding.etTextItem.getText().toString();
            for (int i = 0; i < itemModel.getContents().size(); i++) {
                if (itemModel.getContents().get(i).equals(newContent)) {
                    Toast.makeText(ProjectActivity.this, "Already existed", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            itemModel.addNewContent(newContent);
            statusEditItemAdapter.notifyItemInserted(itemModel.getContents().size());
            dialog.dismiss();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    private void showTextItemPopup(BoardTextItemModel itemModel, String title, int columnPos, int rowPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardTextItemPopupBinding binding = BoardTextItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.textItemTitle.setText(title);
        binding.etTextItem.setText(itemModel.getContent());
        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());

        binding.btnSaveTextItem.setOnClickListener(view -> {
            String newContent = String.valueOf(binding.etTextItem.getText());
            itemModel.setContent(newContent);

            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();
            boardViewModel.updateACell(itemModel).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
                    } else {
                        Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    }

                    loadingDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }
            });
        });

        binding.btnClearTextItem.setOnClickListener((view) -> binding.etTextItem.setText(""));

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showNumberItemPopup(BoardNumberItemModel itemModel, String title, int columnPos, int rowPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardNumberItemPopupBinding binding = BoardNumberItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.textNumberTitle.setText(title);
        binding.etNumberItem.setText(itemModel.getContent());
        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());
        binding.btnSaveNumberItem.setOnClickListener(view -> {
            String newContent = String.valueOf(binding.etNumberItem.getText());
            itemModel.setContent(newContent);

            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();
            boardViewModel.updateACell(itemModel).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
                    } else {
                        Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    }

                    loadingDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }
            });

            boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
            dialog.dismiss();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showTimelineItemPopup(BoardTimelineItemModel itemModel, String title, int columnPos, int rowPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardTimelineItemPopupBinding binding = BoardTimelineItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        final AtomicInteger dialogStartYear = new AtomicInteger(-1);
        final AtomicInteger dialogStartMonth = new AtomicInteger(-1);
        final AtomicInteger dialogStartDay = new AtomicInteger(-1);
        final AtomicInteger dialogEndYear = new AtomicInteger(-1);
        final AtomicInteger dialogEndMonth = new AtomicInteger(-1);
        final AtomicInteger dialogEndDay = new AtomicInteger(-1);

        if (!itemModel.getContent().isEmpty()) {
            dialogStartDay.set(itemModel.getStartDay());
            dialogStartMonth.set(itemModel.getStartMonth());
            dialogStartYear.set(itemModel.getStartYear());
            dialogEndDay.set(itemModel.getEndDay());
            dialogEndMonth.set(itemModel.getEndMonth());
            dialogEndYear.set(itemModel.getEndYear());
            binding.tvTimelineValue.setText(itemModel.getContent());
            binding.tvAddTimelineTitle.setText("Clear");
            binding.tvAddTimelineTitle.setOnClickListener((view) -> {
                dialogStartDay.set(-1);
                dialogStartMonth.set(-1);
                dialogStartYear.set(-1);
                dialogEndDay.set(-1);
                dialogEndMonth.set(-1);
                dialogEndYear.set(-1);
                binding.tvTimelineValue.setText("");
                binding.tvAddTimelineTitle.setText("Add time");
                binding.tvAddTimelineTitle.setOnClickListener(null);
            });
        }

        binding.tvTimelineItemTitle.setText(title);
        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());

        binding.btnSaveTimelineItem.setOnClickListener(view -> {
            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();

            // TODO: The function expects no problems or exceptions, should not update the item if the call failed
            itemModel.setStartYear(dialogStartYear.get());
            itemModel.setStartMonth(dialogStartMonth.get());
            itemModel.setStartDay(dialogStartDay.get());
            itemModel.setEndYear(dialogEndYear.get());
            itemModel.setEndMonth(dialogEndMonth.get());
            itemModel.setEndDay(dialogEndDay.get());

            boardViewModel.updateACell(itemModel).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
                    } else {
                        Toast.makeText(ProjectActivity.this, "Unable to save the cell", Toast.LENGTH_LONG).show();
                    }
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProjectActivity.this, "Unable to save the cell", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }
            });
        });

        binding.addTimeContainer.setOnClickListener((view) -> {
            MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(Pair.create(
                        CustomUtils.getTimeInMillis(dialogStartDay.get(), dialogStartMonth.get(), dialogStartYear.get()),
                        CustomUtils.getTimeInMillis(dialogEndDay.get(), dialogEndMonth.get(), dialogEndYear.get())
                ))
                .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String startDate = Instant
                            .ofEpochMilli(selection.first)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    String[] startData = startDate.split("-");
                    dialogStartDay.set(Integer.parseInt(startData[0]));
                    dialogStartMonth.set(Integer.parseInt(startData[1]));
                    dialogStartYear.set(Integer.parseInt(startData[2]));

                    String endDate = Instant
                            .ofEpochMilli(selection.second)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    String[] endData = endDate.split("-");
                    dialogEndDay.set(Integer.parseInt(endData[0]));
                    dialogEndMonth.set(Integer.parseInt(endData[1]));
                    dialogEndYear.set(Integer.parseInt(endData[2]));
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(selection.first);
                    dialogStartDay.set(calendar.get(Calendar.DAY_OF_MONTH));
                    dialogStartMonth.set(calendar.get(Calendar.MONTH));
                    dialogStartYear.set(calendar.get(Calendar.YEAR));

                    calendar.setTimeInMillis(selection.second);
                    dialogEndDay.set(calendar.get(Calendar.DAY_OF_MONTH));
                    dialogEndMonth.set(calendar.get(Calendar.MONTH));
                    dialogEndYear.set(calendar.get(Calendar.YEAR));
                }

                String finalContent = "";
                if (dialogStartDay.get() != -1 && dialogStartMonth.get() != -1 && dialogStartYear.get() != -1) {
                    if (dialogStartDay.get() == dialogEndDay.get() && dialogStartMonth.get() == dialogStartMonth.get() && dialogStartYear.get() == dialogEndYear.get()) {

                    } else if (dialogStartMonth.get() == dialogStartMonth.get() && dialogStartYear.get() == dialogEndYear.get())
                        finalContent += String.format(Locale.US, "%d", dialogStartDay.get());
                    else if (dialogStartYear.get() == dialogEndYear.get()) {
                        finalContent += String.format(Locale.US, "%s %d", CustomUtils.convertIntToMonth(dialogStartMonth.get()), dialogStartDay.get());
                    } else finalContent += String.format(Locale.US, "%s %d, %d", CustomUtils.convertIntToMonth(dialogStartMonth.get()), dialogStartDay.get(), dialogStartYear.get());

                    if (finalContent.isEmpty())
                        finalContent += String.format(Locale.US, "%s %d, %d", CustomUtils.convertIntToMonth(dialogStartMonth.get()), dialogEndDay.get(), dialogEndYear.get());
                    else finalContent += String.format(Locale.US, " - %s %d, %d", CustomUtils.convertIntToMonth(dialogStartMonth.get()), dialogEndDay.get(), dialogEndYear.get());
                }

                binding.tvTimelineValue.setText(finalContent);
                binding.tvAddTimelineTitle.setText("Clear");
                binding.tvAddTimelineTitle.setOnClickListener((lolView) -> {
                    dialogStartDay.set(-1);
                    dialogStartMonth.set(-1);
                    dialogStartYear.set(-1);
                    dialogEndDay.set(-1);
                    dialogEndMonth.set(-1);
                    dialogEndYear.set(-1);
                    binding.tvTimelineValue.setText("");
                    binding.tvAddTimelineTitle.setText("Add time");
                    binding.tvAddTimelineTitle.setOnClickListener(null);
                });
            });

            materialDatePicker.show(getSupportFragmentManager(), "I DONT KNOW WHAT THIS IS");
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showDateItemPopup(@NonNull BoardDateItemModel itemModel, String title, int columnPos, int rowPos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardDateItemPopupBinding binding = BoardDateItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        final AtomicInteger dialogYear = new AtomicInteger(itemModel.getYear());
        final AtomicInteger dialogMonth = new AtomicInteger(itemModel.getMonth());
        final AtomicInteger dialogDay = new AtomicInteger(itemModel.getDay());
        final AtomicInteger dialogHour = new AtomicInteger(itemModel.getHour());
        final AtomicInteger dialogMinute = new AtomicInteger(itemModel.getMinute());

        if (!itemModel.getDate().isEmpty()) {
            binding.tvDateValue.setText(itemModel.getDate());
            binding.tvAddDateTitle.setText("Clear date");
            binding.tvAddDateTitle.setOnClickListener((view) -> {
                dialogYear.set(-1);
                dialogMonth.set(-1);
                dialogDay.set(-1);
                binding.tvDateValue.setText("");
                binding.tvAddDateTitle.setText("Add date");
                binding.tvAddDateTitle.setOnClickListener(null);
            });
        }
        if (!itemModel.getTime().isEmpty()) {
            binding.tvTimeValue.setText(itemModel.getTime());
            binding.tvAddTimeTitle.setText("Clear time");
            binding.tvAddTimeTitle.setOnClickListener((view) -> {
                dialogHour.set(-1);
                dialogMinute.set(-1);
                binding.tvTimeValue.setText("");
                binding.tvAddTimeTitle.setText("Add time");
                binding.tvAddTimeTitle.setOnClickListener(null);
            });
        }

        binding.tvDateItemTitle.setText(title);
        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());
        binding.btnSaveDateItem.setOnClickListener(view -> {
            itemModel.setYear(dialogYear.get());
            itemModel.setMonth(dialogMonth.get());
            itemModel.setDay(dialogDay.get());
            itemModel.setHour(dialogHour.get());
            itemModel.setMinute(dialogMinute.get());

            Dialog loadingDialog = DialogUtils.GetLoadingDialog(ProjectActivity.this);
            loadingDialog.show();
            Call<Void> cellUpdateCall = boardViewModel.updateACell(itemModel);
            cellUpdateCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        boardAdapter.changeCellItem(columnPos, rowPos, itemModel);
                    } else {
                        Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    }

                    loadingDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProjectActivity.this, "Unable to update the cell", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    dialog.dismiss();
                }
            });

        });

        binding.dateItemDateContainer.setOnClickListener((view) -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProjectActivity.this,
                    null,
                    dialogYear.get() == -1 ? calendar.get(Calendar.YEAR) : dialogYear.get(),
                    dialogMonth.get() == -1 ? calendar.get(Calendar.MONTH) : dialogMonth.get(),
                    dialogDay.get() == - 1 ? calendar.get(Calendar.DAY_OF_MONTH) : dialogDay.get()
            );
            datePickerDialog.setOnDateSetListener((datePicker, newYear, newMonth, newDay) -> {
                dialogYear.set(newYear);
                dialogMonth.set(newMonth);
                dialogDay.set(newDay);
                binding.tvDateValue.setText(String.format(Locale.US,"%s %d, %d", CustomUtils.convertIntToMonth(dialogMonth.get()), dialogDay.get(), dialogYear.get()));
                binding.tvAddDateTitle.setText("Clear date");
                binding.tvAddDateTitle.setOnClickListener((titleView) -> {
                    dialogYear.set(-1);
                    dialogMonth.set(-1);
                    dialogDay.set(-1);
                    binding.tvDateValue.setText("");
                    binding.tvAddDateTitle.setText("Add date");
                    binding.tvAddDateTitle.setOnClickListener(null);
                });
            });
            datePickerDialog.show();
        });

        binding.dateItemTimeContainer.setOnClickListener((view) -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(ProjectActivity.this,
                    (timePicker, newHour, newMinute) -> {
                        dialogHour.set(newHour);
                        dialogMinute.set(newMinute);
                        binding.tvTimeValue.setText(String.format(Locale.US, "%02d:%02d", dialogHour.get(), dialogMinute.get()));
                        binding.tvAddTimeTitle.setText("Clear time");
                        binding.tvAddTimeTitle.setOnClickListener((titleView) -> {
                            dialogHour.set(-1);
                            dialogMinute.set(-1);
                            binding.tvTimeValue.setText("");
                            binding.tvAddTimeTitle.setText("Add time");
                            binding.tvAddTimeTitle.setOnClickListener(null);
                        });
                    },
                    dialogHour.get() == -1 ? calendar.get(Calendar.HOUR_OF_DAY) : dialogHour.get(),
                    dialogMinute.get() == - 1 ? calendar.get(Calendar.MINUTE) : dialogMinute.get(),
                    true
            ).show();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showAddBoardItemPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BoardAddItemPopupBinding binding = BoardAddItemPopupBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.btnClosePopup.setOnClickListener((view) -> dialog.dismiss());

        binding.btnAddTextItem.setOnClickListener((view) -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.Text);
        });

        binding.btnAddUserItem.setOnClickListener((view) -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.User);
        });

        binding.btnAddStatusItem.setOnClickListener((view) -> {
            boardViewModel.createNewColumn((BoardColumnHeaderModel.ColumnType.Status));
        });

        binding.btnAddNumberItem.setOnClickListener((view -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.Number);
        }));

        binding.btnAddUpdateItem.setOnClickListener((view -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.Update);
        }));

        binding.btnAddCheckboxItem.setOnClickListener((view -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.Checkbox);
        }));

        binding.btnAddDateItem.setOnClickListener((view) -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.Date);
        });

        binding.btnAddTimelineItem.setOnClickListener((view) -> {
            boardViewModel.createNewColumn(BoardColumnHeaderModel.ColumnType.TimeLine);
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupAnimationBottom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }
}